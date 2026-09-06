package org.zkaleejoo.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.database.ClanStorage;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.models.ClanFlag;
import org.zkaleejoo.models.ClanPlayer;
import org.zkaleejoo.models.ClanRole;
import org.zkaleejoo.models.TopSortType;
import org.zkaleejoo.utils.MessageUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClanManager {

    private final OnlyClans plugin;
    private final ClanStorage storage;

    private final Map<String, Clan> clans = new HashMap<>();
    private final Map<UUID, String> playerClanMap = new HashMap<>();
    private final Set<UUID> pendingCreations = new HashSet<>();
    private final Map<UUID, String> pendingInvites = new HashMap<>();
    private final Set<UUID> spyModeUsers = new HashSet<>();
    private final Map<String, Set<String>> pendingAlliances = new ConcurrentHashMap<>();

    public ClanManager(OnlyClans plugin, ClanStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public boolean isSpyMode(UUID uuid) {
        return spyModeUsers.contains(uuid);
    }

    public void setSpyMode(UUID uuid, boolean enabled) {
        if (enabled) {
            spyModeUsers.add(uuid);
        } else {
            spyModeUsers.remove(uuid);
        }
    }

    public boolean toggleSpyMode(UUID uuid) {
        if (spyModeUsers.contains(uuid)) {
            spyModeUsers.remove(uuid);
            return false;
        } else {
            spyModeUsers.add(uuid);
            return true;
        }
    }

    public void loadClans() {
        clans.clear();
        playerClanMap.clear();

        Map<String, Clan> loaded = storage.loadAllClans();
        clans.putAll(loaded);

        for (Clan clan : clans.values()) {
            for (UUID uuid : clan.getMembers().keySet()) {
                playerClanMap.put(uuid, clan.getName().toLowerCase());
            }
        }
    }

    public boolean createClan(Player owner, String name, String tag) {
        String key = name.toLowerCase();

        if (clans.containsKey(key)) {
            owner.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getMessage("name-exists",
                            "&cA clan with that name already exists.")));
            return false;
        }

        if (playerClanMap.containsKey(owner.getUniqueId())) {
            owner.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("already-in-clan", "&cYou are already in a clan.")));
            return false;
        }

        Clan clan = new Clan(name, tag, owner.getUniqueId());
        ClanPlayer clanPlayer = new ClanPlayer(owner.getUniqueId(), name, ClanRole.LEADER);
        clan.addMember(clanPlayer);

        clans.put(key, clan);
        playerClanMap.put(owner.getUniqueId(), key);

        storage.saveClan(clan);
        storage.saveClanPlayer(clanPlayer);

        owner.sendMessage(MessageUtils.getColoredMessage(
                plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                        .getMessage("clan-created", "&aClan '&f{clan}&a' created successfully!")
                        .replace("{clan}", name)));
        return true;
    }

    public void disbandClan(Clan clan, Player leader) {
        String key = clan.getName().toLowerCase();

        for (UUID memberUuid : clan.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberUuid);
            if (member != null && member.isOnline()) {
                member.sendMessage(MessageUtils.getColoredMessage(
                        plugin.getMainConfigManager().getPrefix()
                                + plugin.getMainConfigManager()
                                        .getMessage("clan-disbanded", "&cThe clan '&f{clan}&c' has been disbanded.")
                                        .replace("{clan}", clan.getName())));
            }
            playerClanMap.remove(memberUuid);
        }

        clans.remove(key);
        storage.deleteClan(clan.getName());
    }

    public void invitePlayer(Player inviter, Player target) {
        Clan clan = getClanByPlayer(inviter.getUniqueId());
        if (clan == null) {
            inviter.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix()
                            + plugin.getMainConfigManager().getMessage("not-in-clan", "&cYou are not in a clan.")));
            return;
        }

        ClanPlayer cp = clan.getMember(inviter.getUniqueId());
        boolean canInvite = cp != null && (cp.hasRoleAtLeast(ClanRole.MODERATOR) || clan.isMemberInvites());
        if (!canInvite) {
            inviter.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("no-invite-permission", "&cYou don't have permission to invite players.")));
            return;
        }

        int maxMembers = plugin.getClanLevelManager() != null
                ? plugin.getClanLevelManager().getMaxMembers(clan.getLevel())
                : 8;
        if (clan.getMemberCount() >= maxMembers) {
            inviter.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("clan-full",
                                    "&cYour clan has reached its maximum member limit ({current}/{max}) for its current level. Level up your clan to unlock more slots!")
                            .replace("{current}", String.valueOf(clan.getMemberCount()))
                            .replace("{max}", String.valueOf(maxMembers))));
            return;
        }

        if (playerClanMap.containsKey(target.getUniqueId())) {
            inviter.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("target-already-in-clan", "&cThat player is already in a clan.")));
            return;
        }

        if (pendingInvites.containsKey(target.getUniqueId())) {
            inviter.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("target-has-invite", "&cThat player already has a pending invite.")));
            return;
        }

        pendingInvites.put(target.getUniqueId(), clan.getName());
        clan.addInvite(target.getUniqueId());

        inviter.sendMessage(MessageUtils.getColoredMessage(
                plugin.getMainConfigManager().getPrefix()
                        + plugin.getMainConfigManager().getMessage("invite-sent", "&aInvitation sent to &f{player}&a.")
                                .replace("{player}", target.getName())));
        target.sendMessage(MessageUtils.getColoredMessage(
                plugin.getMainConfigManager().getPrefix()
                        + plugin.getMainConfigManager().getMessage("invite-received",
                                "&aYou have been invited to join &f{clan}&a. Type &e/clan accept &ato join or &c/clan deny &ato decline.")
                                .replace("{clan}", clan.getName())));

        String acceptBtnText = plugin.getMainConfigManager().getMessage("invite-button-accept", " [ACCEPT] ");
        String denyBtnText = plugin.getMainConfigManager().getMessage("invite-button-deny", " [DENY] ");
        String acceptHoverText = plugin.getMainConfigManager()
                .getMessage("invite-hover-accept", "&aClick to join &f{clan}")
                .replace("{clan}", clan.getName());
        String denyHoverText = plugin.getMainConfigManager().getMessage("invite-hover-deny", "&cClick to decline");

        Component acceptButton = MessageUtils.toComponent(acceptBtnText)
                .decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/clan accept"))
                .hoverEvent(HoverEvent.showText(MessageUtils.toComponent(acceptHoverText)));

        Component denyButton = MessageUtils.toComponent(denyBtnText)
                .decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/clan deny"))
                .hoverEvent(HoverEvent.showText(MessageUtils.toComponent(denyHoverText)));

        MessageUtils.sendCenteredButtons(target, acceptButton, denyButton);
    }

    public void acceptInvite(Player player) {
        String clanName = pendingInvites.remove(player.getUniqueId());
        if (clanName == null) {
            player.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("no-pending-invites", "&cYou don't have any pending invitations.")));
            return;
        }

        Clan clan = clans.get(clanName.toLowerCase());
        if (clan == null) {
            player.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("clan-not-exists", "&cThat clan no longer exists.")));
            return;
        }

        int maxMembers = plugin.getClanLevelManager() != null
                ? plugin.getClanLevelManager().getMaxMembers(clan.getLevel())
                : 8;
        if (clan.getMemberCount() >= maxMembers) {
            player.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("clan-full-target",
                                    "&cThat clan has reached its maximum member limit ({current}/{max}) for its current level.")
                            .replace("{current}", String.valueOf(clan.getMemberCount()))
                            .replace("{max}", String.valueOf(maxMembers))));
            return;
        }

        clan.removeInvite(player.getUniqueId());
        ClanPlayer cp = new ClanPlayer(player.getUniqueId(), clanName, ClanRole.MEMBER);
        clan.addMember(cp);
        playerClanMap.put(player.getUniqueId(), clanName.toLowerCase());

        storage.saveClanPlayer(cp);

        player.sendMessage(MessageUtils.getColoredMessage(
                plugin.getMainConfigManager().getPrefix()
                        + plugin.getMainConfigManager()
                                .getMessage("joined-clan", "&aYou have joined the clan '&f{clan}&a'!")
                                .replace("{clan}", clanName)));

        broadcastToClan(clan, plugin.getMainConfigManager().getPrefix()
                + plugin.getMainConfigManager().getMessage("player-joined", "&a{player} has joined the clan!")
                        .replace("{player}", player.getName()));
    }

    public void denyInvite(Player player) {
        String clanName = pendingInvites.remove(player.getUniqueId());
        if (clanName == null) {
            player.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("no-pending-invites", "&cYou don't have any pending invitations.")));
            return;
        }

        Clan clan = clans.get(clanName.toLowerCase());
        if (clan != null) {
            clan.removeInvite(player.getUniqueId());
        }

        player.sendMessage(MessageUtils.getColoredMessage(
                plugin.getMainConfigManager().getPrefix()
                        + plugin.getMainConfigManager().getMessage("invite-denied", "&cInvitation denied.")));
    }

    public boolean hasOnlineStaff(Clan clan) {
        if (clan == null)
            return false;
        for (ClanPlayer cp : clan.getMembers().values()) {
            if (cp.hasRoleAtLeast(ClanRole.MODERATOR)) {
                Player p = Bukkit.getPlayer(cp.getUuid());
                if (p != null && p.isOnline()) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Player> getOnlineStaff(Clan clan) {
        List<Player> staff = new ArrayList<>();
        if (clan == null)
            return staff;
        for (ClanPlayer cp : clan.getMembers().values()) {
            if (cp.hasRoleAtLeast(ClanRole.MODERATOR)) {
                Player p = Bukkit.getPlayer(cp.getUuid());
                if (p != null && p.isOnline()) {
                    staff.add(p);
                }
            }
        }
        return staff;
    }

    public void requestToJoinClan(Player applicant, Clan clan) {
        if (clan == null) {
            applicant.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix()
                            + plugin.getMainConfigManager().getMessage("clan-not-exists",
                                    "&cThat clan no longer exists.")));
            return;
        }

        if (isInClan(applicant.getUniqueId())) {
            applicant.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("already-in-clan", "&cYou are already in a clan.")));
            return;
        }

        int maxMembers = plugin.getClanLevelManager() != null
                ? plugin.getClanLevelManager().getMaxMembers(clan.getLevel())
                : 8;
        if (clan.getMemberCount() >= maxMembers) {
            applicant.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("clan-full-target",
                                    "&cThat clan has reached its maximum member limit ({current}/{max}) for its current level.")
                            .replace("{current}", String.valueOf(clan.getMemberCount()))
                            .replace("{max}", String.valueOf(maxMembers))));
            return;
        }

        if (clan.isOpenJoin()) {
            joinOpenClan(applicant, clan);
            return;
        }

        if (clan.hasJoinRequest(applicant.getUniqueId())) {
            applicant.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("request-already-sent", "&cYou have already requested to join this clan.")));
            return;
        }

        List<Player> onlineStaff = getOnlineStaff(clan);
        if (onlineStaff.isEmpty()) {
            applicant.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("request-no-staff-online",
                                    "&cCannot send request: No leader or moderator of '&f{clan}&c' is online.")
                            .replace("{clan}", clan.getName())));
            return;
        }

        clan.addJoinRequest(applicant.getUniqueId());

        applicant.sendMessage(MessageUtils.getColoredMessage(
                plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                        .getMessage("request-sent",
                                "&aJoin request sent to '&f{clan}&a'! Waiting for a leader or moderator to accept.")
                        .replace("{clan}", clan.getName())));

        String acceptBtnText = plugin.getMainConfigManager().getMessage("request-button-accept", " [ACCEPT] ");
        String denyBtnText = plugin.getMainConfigManager().getMessage("request-button-deny", " [DENY] ");
        String acceptHoverText = plugin.getMainConfigManager()
                .getMessage("request-hover-accept", "&aClick to accept &f{player}")
                .replace("{player}", applicant.getName());
        String denyHoverText = plugin.getMainConfigManager()
                .getMessage("request-hover-deny", "&cClick to deny &f{player}")
                .replace("{player}", applicant.getName());

        Component acceptButton = MessageUtils.toComponent(acceptBtnText)
                .decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/clan acceptrequest " + applicant.getName()))
                .hoverEvent(HoverEvent.showText(MessageUtils.toComponent(acceptHoverText)));

        Component denyButton = MessageUtils.toComponent(denyBtnText)
                .decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/clan denyrequest " + applicant.getName()))
                .hoverEvent(HoverEvent.showText(MessageUtils.toComponent(denyHoverText)));

        String notifMsg = plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                .getMessage("request-received", "&aPlayer &f{player}&a has requested to join your clan '&f{clan}&a'.")
                .replace("{player}", applicant.getName())
                .replace("{clan}", clan.getName());

        for (Player staffMember : onlineStaff) {
            staffMember.sendMessage(MessageUtils.getColoredMessage(notifMsg));
            MessageUtils.sendCenteredButtons(staffMember, acceptButton, denyButton);
        }
    }

    public void acceptJoinRequest(Player staff, String applicantName) {
        Clan clan = getClanByPlayer(staff.getUniqueId());
        if (clan == null) {
            staff.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix()
                            + plugin.getMainConfigManager().getMessage("not-in-clan", "&cYou are not in a clan.")));
            return;
        }

        ClanPlayer staffCp = clan.getMember(staff.getUniqueId());
        if (staffCp == null || !staffCp.hasRoleAtLeast(ClanRole.MODERATOR)) {
            staff.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("no-invite-permission", "&cYou don't have permission to accept members.")));
            return;
        }

        UUID targetUuid = null;
        String resolvedName = applicantName;

        for (UUID uuid : clan.getJoinRequests()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.getName().equalsIgnoreCase(applicantName)) {
                targetUuid = uuid;
                resolvedName = p.getName();
                break;
            }
            OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
            if (op.getName() != null && op.getName().equalsIgnoreCase(applicantName)) {
                targetUuid = uuid;
                resolvedName = op.getName();
                break;
            }
        }

        if (targetUuid == null) {
            staff.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("request-no-pending", "&cThere is no pending join request from '&f{player}&c'.")
                            .replace("{player}", applicantName)));
            return;
        }

        clan.removeJoinRequest(targetUuid);

        int maxMembers = plugin.getClanLevelManager() != null
                ? plugin.getClanLevelManager().getMaxMembers(clan.getLevel())
                : 8;
        if (clan.getMemberCount() >= maxMembers) {
            staff.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("clan-full",
                                    "&cYour clan has reached its maximum member limit ({current}/{max}) for its current level. Level up your clan to unlock more slots!")
                            .replace("{current}", String.valueOf(clan.getMemberCount()))
                            .replace("{max}", String.valueOf(maxMembers))));
            return;
        }

        if (isInClan(targetUuid)) {
            staff.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("target-already-in-clan", "&cThat player is already in a clan.")));
            return;
        }

        ClanPlayer newMember = new ClanPlayer(targetUuid, clan.getName(), ClanRole.MEMBER);
        clan.addMember(newMember);
        playerClanMap.put(targetUuid, clan.getName().toLowerCase());

        storage.saveClanPlayer(newMember);

        staff.sendMessage(MessageUtils.getColoredMessage(
                plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                        .getMessage("request-accepted-staff", "&aYou accepted &f{player}&a into the clan.")
                        .replace("{player}", resolvedName)));

        broadcastToClan(clan, plugin.getMainConfigManager().getPrefix()
                + plugin.getMainConfigManager().getMessage("player-joined", "&a{player} has joined the clan!")
                        .replace("{player}", resolvedName));

        Player targetPlayer = Bukkit.getPlayer(targetUuid);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            targetPlayer.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("request-accepted-player", "&aYour request to join '&f{clan}&a' was accepted!")
                            .replace("{clan}", clan.getName())));
        }
    }

    public void denyJoinRequest(Player staff, String applicantName) {
        Clan clan = getClanByPlayer(staff.getUniqueId());
        if (clan == null) {
            staff.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix()
                            + plugin.getMainConfigManager().getMessage("not-in-clan", "&cYou are not in a clan.")));
            return;
        }

        ClanPlayer staffCp = clan.getMember(staff.getUniqueId());
        if (staffCp == null || !staffCp.hasRoleAtLeast(ClanRole.MODERATOR)) {
            staff.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("no-invite-permission", "&cYou don't have permission to manage members.")));
            return;
        }

        UUID targetUuid = null;
        String resolvedName = applicantName;

        for (UUID uuid : clan.getJoinRequests()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.getName().equalsIgnoreCase(applicantName)) {
                targetUuid = uuid;
                resolvedName = p.getName();
                break;
            }
            OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
            if (op.getName() != null && op.getName().equalsIgnoreCase(applicantName)) {
                targetUuid = uuid;
                resolvedName = op.getName();
                break;
            }
        }

        if (targetUuid == null) {
            staff.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("request-no-pending", "&cThere is no pending join request from '&f{player}&c'.")
                            .replace("{player}", applicantName)));
            return;
        }

        clan.removeJoinRequest(targetUuid);

        staff.sendMessage(MessageUtils.getColoredMessage(
                plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                        .getMessage("request-denied-staff", "&cYou denied '&f{player}&c's request to join.")
                        .replace("{player}", resolvedName)));

        Player targetPlayer = Bukkit.getPlayer(targetUuid);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            targetPlayer.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("request-denied-player", "&cYour request to join '&f{clan}&c' was declined.")
                            .replace("{clan}", clan.getName())));
        }
    }

    public void leaveClan(Player player) {
        Clan clan = getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix()
                            + plugin.getMainConfigManager().getMessage("not-in-clan", "&cYou are not in a clan.")));
            return;
        }

        ClanPlayer cp = clan.getMember(player.getUniqueId());
        if (cp != null && cp.isLeader()) {
            player.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix()
                            + plugin.getMainConfigManager().getMessage("cannot-leave-leader",
                                    "&cYou cannot leave as leader. Transfer ownership or disband the clan.")));
            return;
        }

        clan.removeMember(player.getUniqueId());
        playerClanMap.remove(player.getUniqueId());
        storage.removeClanPlayer(player.getUniqueId());

        player.sendMessage(MessageUtils.getColoredMessage(
                plugin.getMainConfigManager().getPrefix()
                        + plugin.getMainConfigManager()
                                .getMessage("left-clan", "&eYou have left the clan '&f{clan}&e'.")
                                .replace("{clan}", clan.getName())));

        broadcastToClan(clan, plugin.getMainConfigManager().getPrefix()
                + plugin.getMainConfigManager().getMessage("player-left", "&e{player} has left the clan.")
                        .replace("{player}", player.getName()));
    }

    public void kickPlayer(Player kicker, Player target) {
        Clan clan = getClanByPlayer(kicker.getUniqueId());
        if (clan == null)
            return;

        ClanPlayer kickerCp = clan.getMember(kicker.getUniqueId());
        ClanPlayer targetCp = clan.getMember(target.getUniqueId());

        if (kickerCp == null || targetCp == null)
            return;

        if (!kickerCp.hasRoleAtLeast(ClanRole.MODERATOR)) {
            kicker.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("no-kick-permission", "&cYou don't have permission to kick players.")));
            return;
        }

        if (targetCp.getRole().getWeight() >= kickerCp.getRole().getWeight()) {
            kicker.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("cannot-kick-higher", "&cYou cannot kick someone with equal or higher rank.")));
            return;
        }

        clan.removeMember(target.getUniqueId());
        playerClanMap.remove(target.getUniqueId());
        storage.removeClanPlayer(target.getUniqueId());

        target.sendMessage(MessageUtils.getColoredMessage(
                plugin.getMainConfigManager().getPrefix()
                        + plugin.getMainConfigManager()
                                .getMessage("kicked-from-clan", "&cYou have been kicked from '&f{clan}&c'.")
                                .replace("{clan}", clan.getName())));
        broadcastToClan(clan, plugin.getMainConfigManager().getPrefix()
                + plugin.getMainConfigManager()
                        .getMessage("player-kicked", "&c{player} has been kicked from the clan.")
                        .replace("{player}", target.getName()));
    }

    public void promotePlayer(Player promoter, Player target) {
        Clan clan = getClanByPlayer(promoter.getUniqueId());
        if (clan == null)
            return;

        ClanPlayer promoterCp = clan.getMember(promoter.getUniqueId());
        ClanPlayer targetCp = clan.getMember(target.getUniqueId());

        if (promoterCp == null || !promoterCp.isLeader()) {
            promoter.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("promote-leader-only", "&cOnly the leader can promote members.")));
            return;
        }

        if (targetCp == null) {
            promoter.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("player-not-in-clan", "&cThat player is not in your clan.")));
            return;
        }

        if (targetCp.getRole() == ClanRole.MODERATOR) {
            promoter.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("already-moderator", "&cThat player is already a Moderator.")));
            return;
        }

        targetCp.setRole(ClanRole.MODERATOR);
        storage.updateClanPlayerRole(target.getUniqueId(), ClanRole.MODERATOR);

        promoter.sendMessage(MessageUtils.getColoredMessage(
                plugin.getMainConfigManager().getPrefix()
                        + plugin.getMainConfigManager()
                                .getMessage("promoted-other", "&a{player} has been promoted to Moderator.")
                                .replace("{player}", target.getName())));
        target.sendMessage(MessageUtils.getColoredMessage(
                plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getMessage("promoted-self",
                        "&aYou have been promoted to Moderator!")));
    }

    public void demotePlayer(Player demoter, Player target) {
        Clan clan = getClanByPlayer(demoter.getUniqueId());
        if (clan == null)
            return;

        ClanPlayer demoterCp = clan.getMember(demoter.getUniqueId());
        ClanPlayer targetCp = clan.getMember(target.getUniqueId());

        if (demoterCp == null || !demoterCp.isLeader()) {
            demoter.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("demote-leader-only", "&cOnly the leader can demote members.")));
            return;
        }

        if (targetCp == null || targetCp.getRole() == ClanRole.MEMBER) {
            demoter.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("already-member", "&cThat player is already a Member.")));
            return;
        }

        targetCp.setRole(ClanRole.MEMBER);
        storage.updateClanPlayerRole(target.getUniqueId(), ClanRole.MEMBER);

        demoter.sendMessage(MessageUtils.getColoredMessage(
                plugin.getMainConfigManager().getPrefix()
                        + plugin.getMainConfigManager()
                                .getMessage("demoted-other", "&e{player} has been demoted to Member.")
                                .replace("{player}", target.getName())));
        target.sendMessage(MessageUtils.getColoredMessage(
                plugin.getMainConfigManager().getPrefix()
                        + plugin.getMainConfigManager().getMessage("demoted-self",
                                "&cYou have been demoted to Member.")));
    }

    public void joinOpenClan(Player player, Clan clan) {
        if (clan == null)
            return;
        if (isInClan(player.getUniqueId())) {
            player.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("already-in-clan", "&cYou are already in a clan.")));
            return;
        }

        int maxMembers = plugin.getClanLevelManager() != null
                ? plugin.getClanLevelManager().getMaxMembers(clan.getLevel())
                : 8;
        if (clan.getMemberCount() >= maxMembers) {
            player.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("clan-full-target",
                                    "&cThat clan has reached its maximum member limit ({current}/{max}) for its current level.")
                            .replace("{current}", String.valueOf(clan.getMemberCount()))
                            .replace("{max}", String.valueOf(maxMembers))));
            return;
        }

        clan.removeJoinRequest(player.getUniqueId());
        clan.removeInvite(player.getUniqueId());
        pendingInvites.remove(player.getUniqueId());

        ClanPlayer cp = new ClanPlayer(player.getUniqueId(), clan.getName(), ClanRole.MEMBER);
        clan.addMember(cp);
        playerClanMap.put(player.getUniqueId(), clan.getName().toLowerCase());
        storage.saveClanPlayer(cp);

        player.sendMessage(MessageUtils.getColoredMessage(
                plugin.getMainConfigManager().getPrefix()
                        + plugin.getMainConfigManager()
                                .getMessage("clan-joined-open", "&aYou have joined the open clan '&f{clan}&a'!")
                                .replace("{clan}", clan.getName())));

        broadcastToClan(clan, plugin.getMainConfigManager().getPrefix()
                + plugin.getMainConfigManager().getMessage("player-joined", "&a{player} has joined the clan!")
                        .replace("{player}", player.getName()));
    }

    public void toggleFriendlyFire(Clan clan) {
        clan.setFriendlyFire(!clan.isFriendlyFire());
        storage.updateClan(clan);

        String lang = plugin.getMainConfigManager().getSelectedLanguage();
        String status = plugin.getPlaceholderManager() != null
                ? plugin.getPlaceholderManager().getFriendlyFireBadge(clan.isFriendlyFire(), lang)
                : (clan.isFriendlyFire() ? "&aON" : "&cOFF");
        broadcastToClan(clan, plugin.getMainConfigManager().getPrefix()
                + plugin.getMainConfigManager()
                        .getMessage("friendly-fire-toggled", "&eFriendly Fire is now {status}&e.")
                        .replace("{status}", status));
    }

    public boolean toggleFlag(Clan clan, ClanFlag flag, Player actor) {
        if (clan == null || flag == null)
            return false;
        return setFlag(clan, flag, !clan.getFlag(flag), actor);
    }

    public boolean setFlag(Clan clan, ClanFlag flag, boolean newVal, Player actor) {
        if (clan == null || flag == null || actor == null)
            return false;

        if (flag.isAdminOnly()) {
            if (!actor.hasPermission("onlyclans.admin") && !actor.hasPermission("onlyclans.spy")) {
                actor.sendMessage(MessageUtils.getColoredMessage(
                        plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                .getMessage("flag-admin-only",
                                        "&cOnly server administrators can modify this administrative flag.")));
                return false;
            }
        } else {
            ClanPlayer cp = clan.getMember(actor.getUniqueId());
            boolean canModify = actor.hasPermission("onlyclans.admin")
                    || (cp != null && cp.hasRoleAtLeast(ClanRole.MODERATOR));
            if (!canModify) {
                actor.sendMessage(MessageUtils.getColoredMessage(
                        plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                .getMessage("no-flag-permission",
                                        "&cOnly clan leaders and moderators can modify clan flags.")));
                return false;
            }
        }

        clan.setFlag(flag, newVal);
        storage.updateClan(clan);

        String lang = plugin.getMainConfigManager().getSelectedLanguage();
        String statusText = plugin.getPlaceholderManager() != null
                ? plugin.getPlaceholderManager().getFlagStatus(flag, newVal, lang)
                : (newVal ? "&aON" : "&cOFF");

        if (flag == ClanFlag.SPY_CHAT) {
            String adminMsg = plugin.getMainConfigManager().getPrefix()
                    + plugin.getMainConfigManager()
                            .getMessage("flag-toggled-spy_chat", "&eAdministrative spy monitoring is now {status}&e.")
                            .replace("{status}", statusText);
            actor.sendMessage(MessageUtils.getColoredMessage(adminMsg));
        } else {
            String msgKey = "flag-toggled-" + flag.getKey();
            String broadcastMsg = plugin.getMainConfigManager().getPrefix()
                    + plugin.getMainConfigManager()
                            .getMessage(msgKey, "&eFlag " + flag.getKey() + " is now {status}&e.")
                            .replace("{status}", statusText);
            broadcastToClan(clan, broadcastMsg);
        }

        return true;
    }

    public boolean setClanTag(Player player, Clan clan, String newTag) {
        if (player == null || clan == null || newTag == null) {
            return false;
        }

        ClanPlayer cp = clan.getMember(player.getUniqueId());
        boolean isLeader = (cp != null && cp.isLeader()) || player.hasPermission("onlyclans.admin");
        if (!isLeader) {
            player.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("only-leader-tag", "&cOnly the clan leader can change the clan tag.")));
            return false;
        }

        String stripped = MessageUtils.stripColor(newTag);
        if (stripped == null || stripped.trim().length() < 2 || stripped.trim().length() > 6) {
            player.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("tag-invalid-length", "&cClan tag must be between 2 and 6 characters.")));
            return false;
        }

        String formattedTag = newTag.trim();
        clan.setTag(formattedTag);
        storage.updateClan(clan);

        player.sendMessage(MessageUtils.getColoredMessage(
                plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                        .getMessage("tag-updated", "&aClan tag updated to {tag}&a.")
                        .replace("{tag}", formattedTag)));

        String broadcastMsg = plugin.getMainConfigManager().getPrefix()
                + plugin.getMainConfigManager()
                        .getMessage("tag-updated-broadcast", "&eClan tag was changed to {tag}&e by &f{player}&e.")
                        .replace("{tag}", formattedTag)
                        .replace("{player}", player.getName());
        broadcastToClan(clan, broadcastMsg);

        return true;
    }

    public boolean setClanDisplayName(Player player, Clan clan, String newDisplayName) {
        if (player == null || clan == null || newDisplayName == null) {
            return false;
        }

        ClanPlayer cp = clan.getMember(player.getUniqueId());
        boolean isLeader = (cp != null && cp.isLeader()) || player.hasPermission("onlyclans.admin");
        if (!isLeader) {
            player.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("only-leader-decor", "&cOnly the clan leader can decorate the clan name.")));
            return false;
        }

        String trimmed = newDisplayName.trim();
        if (trimmed.equalsIgnoreCase("reset") || trimmed.equalsIgnoreCase("clear")
                || trimmed.equalsIgnoreCase("none")) {
            clan.setDisplayName(null);
            storage.updateClan(clan);

            player.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("decor-reset", "&aClan display name reset to default.")));

            String broadcastMsg = plugin.getMainConfigManager().getPrefix()
                    + plugin.getMainConfigManager()
                            .getMessage("decor-reset-broadcast",
                                    "&eClan display name was reset to &f{clan}&e by &f{player}&e.")
                            .replace("{clan}", clan.getName())
                            .replace("{player}", player.getName());
            broadcastToClan(clan, broadcastMsg);
            return true;
        }

        String stripped = MessageUtils.stripColor(trimmed);
        if (stripped == null || !stripped.trim().equalsIgnoreCase(clan.getName())) {
            player.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("decor-mismatch",
                                    "&cThe decorated name must match your clan's original name: &f{clan}")
                            .replace("{clan}", clan.getName())));
            return false;
        }

        clan.setDisplayName(trimmed);
        storage.updateClan(clan);

        player.sendMessage(MessageUtils.getColoredMessage(
                plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                        .getMessage("decor-updated", "&aClan display name updated to {name}&a.")
                        .replace("{name}", trimmed)));

        String broadcastMsg = plugin.getMainConfigManager().getPrefix()
                + plugin.getMainConfigManager()
                        .getMessage("decor-updated-broadcast",
                                "&eClan display name was changed to {name}&e by &f{player}&e.")
                        .replace("{name}", trimmed)
                        .replace("{player}", player.getName());
        broadcastToClan(clan, broadcastMsg);

        return true;
    }

    public void sendClanMessage(Clan clan, Player sender, String message) {
        String formatted = plugin.getMainConfigManager().getPrefix()
                + plugin.getMainConfigManager()
                        .getMessage("clan-chat-format", "&b[Clan Chat] &f{player}&7: &f{message}")
                        .replace("{player}", sender.getName()).replace("{message}", message);
        Component comp = MessageUtils.toComponent(formatted);

        for (UUID uuid : clan.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(uuid);
            if (member != null && member.isOnline()) {
                member.sendMessage(comp);
            }
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("onlyclans.spy") || online.hasPermission("onlyclans.admin")) {
                if (!clan.hasMember(online.getUniqueId())) {
                    if (clan.isSpyChat() || isSpyMode(online.getUniqueId())) {
                        String spyFormatted = plugin.getMainConfigManager().getPrefix()
                                + plugin.getMainConfigManager()
                                        .getMessage("clan-chat-spy-format",
                                                "&8[&cClanSpy&8] &7[{clan}] &f{player}&7: &f{message}")
                                        .replace("{clan}", clan.getName())
                                        .replace("{player}", sender.getName())
                                        .replace("{message}", message);
                        online.sendMessage(MessageUtils.toComponent(spyFormatted));
                    }
                }
            }
        }
    }

    public void addPendingCreation(UUID uuid) {
        pendingCreations.add(uuid);
    }

    public boolean hasPendingCreation(UUID uuid) {
        return pendingCreations.contains(uuid);
    }

    public void removePendingCreation(UUID uuid) {
        pendingCreations.remove(uuid);
    }

    public Clan getClanByPlayer(UUID uuid) {
        String key = playerClanMap.get(uuid);
        if (key == null)
            return null;
        return clans.get(key);
    }

    public Clan getClanByName(String name) {
        return clans.get(name.toLowerCase());
    }

    public boolean isInClan(UUID uuid) {
        return playerClanMap.containsKey(uuid);
    }

    public Collection<Clan> getAllClans() {
        return Collections.unmodifiableCollection(clans.values());
    }

    public ClanStorage getStorage() {
        return storage;
    }

    public void registerPvPStats(Player attacker, Player victim) {
        if (attacker == null || victim == null || attacker.equals(victim)) {
            return;
        }

        Clan attackerClan = getClanByPlayer(attacker.getUniqueId());
        Clan victimClan = getClanByPlayer(victim.getUniqueId());

        if (attackerClan == null && victimClan == null) {
            return;
        }

        if (attackerClan != null) {
            if (victimClan == null) {
                attackerClan.addKill();
                storage.updateClan(attackerClan);
            } else if (!attackerClan.getName().equalsIgnoreCase(victimClan.getName())) {
                attackerClan.addKill();
                attackerClan.addRivalKill();
                storage.updateClan(attackerClan);

                victimClan.addDeath();
                storage.updateClan(victimClan);
            }
        } else {
            victimClan.addDeath();
            storage.updateClan(victimClan);
        }
    }

    public List<Clan> getTopClans(TopSortType sortType) {
        return getTopClans(sortType, true);
    }

    public List<Clan> getTopClans(TopSortType sortType, boolean onlyVisible) {
        if (sortType == null) {
            sortType = TopSortType.KDR;
        }
        List<Clan> list = new ArrayList<>();
        for (Clan clan : clans.values()) {
            if (!onlyVisible || clan.isVisibleInList()) {
                list.add(clan);
            }
        }

        switch (sortType) {
            case KDR -> list.sort((a, b) -> {
                int cmp = Double.compare(b.getKDR(), a.getKDR());
                if (cmp != 0)
                    return cmp;
                int killCmp = Integer.compare(b.getKills(), a.getKills());
                if (killCmp != 0)
                    return killCmp;
                return Long.compare(a.getCreatedAt(), b.getCreatedAt());
            });
            case KILLS -> list.sort((a, b) -> {
                int cmp = Integer.compare(b.getKills(), a.getKills());
                if (cmp != 0)
                    return cmp;
                int kdrCmp = Double.compare(b.getKDR(), a.getKDR());
                if (kdrCmp != 0)
                    return kdrCmp;
                return Long.compare(a.getCreatedAt(), b.getCreatedAt());
            });
            case MEMBERS -> list.sort((a, b) -> {
                int cmp = Integer.compare(b.getMemberCount(), a.getMemberCount());
                if (cmp != 0)
                    return cmp;
                int killCmp = Integer.compare(b.getKills(), a.getKills());
                if (killCmp != 0)
                    return killCmp;
                return Double.compare(b.getKDR(), a.getKDR());
            });
        }
        return list;
    }

    public Clan getTopClan(TopSortType sortType, int rank) {
        if (rank <= 0)
            return null;
        List<Clan> top = getTopClans(sortType, true);
        if (rank <= top.size()) {
            return top.get(rank - 1);
        }
        return null;
    }

    public boolean hasPendingAlliance(String targetClan, String fromClan) {
        if (targetClan == null || fromClan == null)
            return false;
        Set<String> set = pendingAlliances.get(targetClan.toLowerCase());
        return set != null && set.contains(fromClan.toLowerCase());
    }

    public void requestAlly(Player requester, Clan targetClan) {
        Clan clan = getClanByPlayer(requester.getUniqueId());
        if (clan == null) {
            requester.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix()
                            + plugin.getMainConfigManager().getMessage("not-in-clan", "&cYou are not in a clan.")));
            return;
        }

        ClanPlayer cp = clan.getMember(requester.getUniqueId());
        if (cp == null || !cp.hasRoleAtLeast(ClanRole.MODERATOR)) {
            requester.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("no-ally-permission",
                                    "&cOnly clan leaders and moderators can manage alliances.")));
            return;
        }

        if (clan.getName().equalsIgnoreCase(targetClan.getName())) {
            requester.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("cannot-ally-self", "&cYou cannot form an alliance with your own clan.")));
            return;
        }

        if (clan.isAlly(targetClan.getName())) {
            requester.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("already-allies", "&cYour clan is already allied with '&f{clan}&c'.")
                            .replace("{clan}", targetClan.getName())));
            return;
        }

        int maxAlliesClan = plugin.getClanLevelManager() != null
                ? plugin.getClanLevelManager().getMaxAllies(clan.getLevel())
                : 0;
        if (maxAlliesClan <= 0) {
            requester.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("ally-level-locked",
                                    "&cAlliances are unlocked at &eClan Level 4&c. Current level: &e{level}")
                            .replace("{level}", String.valueOf(clan.getLevel()))));
            return;
        }

        if (clan.getAllies().size() >= maxAlliesClan) {
            requester.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("ally-limit-reached",
                                    "&cYour clan has reached the maximum ally limit ({current}/{max}) for its current level.")
                            .replace("{current}", String.valueOf(clan.getAllies().size()))
                            .replace("{max}", String.valueOf(maxAlliesClan))));
            return;
        }

        int maxAlliesTarget = plugin.getClanLevelManager() != null
                ? plugin.getClanLevelManager().getMaxAllies(targetClan.getLevel())
                : 0;
        if (maxAlliesTarget <= 0) {
            requester.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("ally-target-level-low",
                                    "&cThe target clan '&f{clan}&c' has not unlocked alliances yet (Requires Level 4).")
                            .replace("{clan}", targetClan.getName())));
            return;
        }

        if (targetClan.getAllies().size() >= maxAlliesTarget) {
            requester.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("ally-target-limit-reached",
                                    "&cThe target clan '&f{clan}&c' has reached its maximum ally limit.")
                            .replace("{clan}", targetClan.getName())));
            return;
        }

        // If target already sent an alliance request to this clan, accept automatically
        if (hasPendingAlliance(clan.getName(), targetClan.getName())) {
            acceptAlly(requester, targetClan);
            return;
        }

        if (hasPendingAlliance(targetClan.getName(), clan.getName())) {
            requester.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("ally-request-already-sent",
                                    "&cYou have already sent an alliance request to '&f{clan}&c'.")
                            .replace("{clan}", targetClan.getName())));
            return;
        }

        pendingAlliances.computeIfAbsent(targetClan.getName().toLowerCase(), k -> new HashSet<>())
                .add(clan.getName().toLowerCase());

        requester.sendMessage(MessageUtils.getColoredMessage(
                plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                        .getMessage("ally-request-sent", "&aAlliance request sent to '&f{clan}&a'.")
                        .replace("{clan}", targetClan.getName())));

        List<Player> targetStaff = getOnlineStaff(targetClan);
        String notifMsg = plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                .getMessage("ally-request-received", "&eThe clan '&f{clan}&e' has proposed a diplomatic alliance!")
                .replace("{clan}", clan.getName());

        String acceptBtnText = plugin.getMainConfigManager().getMessage("invite-button-accept", " [ACCEPT] ");
        String denyBtnText = plugin.getMainConfigManager().getMessage("invite-button-deny", " [DENY] ");
        String acceptHover = plugin.getMainConfigManager()
                .getMessage("ally-hover-accept", "&aClick to accept alliance with &f{clan}")
                .replace("{clan}", clan.getName());
        String denyHover = plugin.getMainConfigManager()
                .getMessage("ally-hover-deny", "&cClick to deny alliance with &f{clan}")
                .replace("{clan}", clan.getName());

        Component acceptButton = MessageUtils.toComponent(acceptBtnText)
                .decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/clan ally accept " + clan.getName()))
                .hoverEvent(HoverEvent.showText(MessageUtils.toComponent(acceptHover)));

        Component denyButton = MessageUtils.toComponent(denyBtnText)
                .decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/clan ally deny " + clan.getName()))
                .hoverEvent(HoverEvent.showText(MessageUtils.toComponent(denyHover)));

        for (Player staff : targetStaff) {
            staff.sendMessage(MessageUtils.getColoredMessage(notifMsg));
            MessageUtils.sendCenteredButtons(staff, acceptButton, denyButton);
        }
    }

    public void acceptAlly(Player staff, Clan targetClan) {
        Clan clan = getClanByPlayer(staff.getUniqueId());
        if (clan == null)
            return;

        ClanPlayer cp = clan.getMember(staff.getUniqueId());
        if (cp == null || !cp.hasRoleAtLeast(ClanRole.MODERATOR)) {
            staff.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("no-ally-permission",
                                    "&cOnly clan leaders and moderators can manage alliances.")));
            return;
        }

        Set<String> set = pendingAlliances.get(clan.getName().toLowerCase());
        if (set == null || !set.remove(targetClan.getName().toLowerCase())) {
            staff.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("ally-no-pending-request",
                                    "&cThere is no pending alliance request from '&f{clan}&c'.")
                            .replace("{clan}", targetClan.getName())));
            return;
        }

        int maxAlliesClan = plugin.getClanLevelManager() != null
                ? plugin.getClanLevelManager().getMaxAllies(clan.getLevel())
                : 0;
        int maxAlliesTarget = plugin.getClanLevelManager() != null
                ? plugin.getClanLevelManager().getMaxAllies(targetClan.getLevel())
                : 0;

        if (clan.getAllies().size() >= maxAlliesClan || targetClan.getAllies().size() >= maxAlliesTarget) {
            staff.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("ally-limit-reached",
                                    "&cOne of the clans has reached its maximum ally limit.")));
            return;
        }

        clan.addAlly(targetClan.getName());
        targetClan.addAlly(clan.getName());

        storage.saveAlly(clan.getName(), targetClan.getName());
        storage.saveAlly(targetClan.getName(), clan.getName());

        String broadcastMsg = plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                .getMessage("ally-formed",
                        "&aAn alliance has been established between '&f{clan1}&a' and '&f{clan2}&a'!")
                .replace("{clan1}", clan.getName())
                .replace("{clan2}", targetClan.getName());

        broadcastToClan(clan, broadcastMsg);
        broadcastToClan(targetClan, broadcastMsg);
    }

    public void denyAlly(Player staff, Clan targetClan) {
        Clan clan = getClanByPlayer(staff.getUniqueId());
        if (clan == null)
            return;

        ClanPlayer cp = clan.getMember(staff.getUniqueId());
        if (cp == null || !cp.hasRoleAtLeast(ClanRole.MODERATOR)) {
            staff.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("no-ally-permission",
                                    "&cOnly clan leaders and moderators can manage alliances.")));
            return;
        }

        Set<String> set = pendingAlliances.get(clan.getName().toLowerCase());
        if (set == null || !set.remove(targetClan.getName().toLowerCase())) {
            staff.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("ally-no-pending-request",
                                    "&cThere is no pending alliance request from '&f{clan}&c'.")
                            .replace("{clan}", targetClan.getName())));
            return;
        }

        staff.sendMessage(MessageUtils.getColoredMessage(
                plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                        .getMessage("ally-request-denied", "&cYou declined the alliance proposal from '&f{clan}&c'.")
                        .replace("{clan}", targetClan.getName())));

        List<Player> targetStaff = getOnlineStaff(targetClan);
        for (Player p : targetStaff) {
            p.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("ally-request-denied-other",
                                    "&cThe clan '&f{clan}&c' has declined your alliance proposal.")
                            .replace("{clan}", clan.getName())));
        }
    }

    public void removeAlly(Player player, Clan targetClan) {
        Clan clan = getClanByPlayer(player.getUniqueId());
        if (clan == null)
            return;

        ClanPlayer cp = clan.getMember(player.getUniqueId());
        if (cp == null || !cp.hasRoleAtLeast(ClanRole.MODERATOR)) {
            player.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("no-ally-permission",
                                    "&cOnly clan leaders and moderators can manage alliances.")));
            return;
        }

        if (!clan.isAlly(targetClan.getName())) {
            player.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("not-allies", "&cYour clan is not allied with '&f{clan}&c'.")
                            .replace("{clan}", targetClan.getName())));
            return;
        }

        clan.removeAlly(targetClan.getName());
        targetClan.removeAlly(clan.getName());

        storage.removeAlly(clan.getName(), targetClan.getName());

        String broadcastMsg = plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                .getMessage("ally-disbanded", "&cThe alliance between '&f{clan1}&c' and '&f{clan2}&c' has ended.")
                .replace("{clan1}", clan.getName())
                .replace("{clan2}", targetClan.getName());

        broadcastToClan(clan, broadcastMsg);
        broadcastToClan(targetClan, broadcastMsg);
    }

    private void broadcastToClan(Clan clan, String message) {
        Component comp = MessageUtils.toComponent(message);
        for (UUID uuid : clan.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(uuid);
            if (member != null && member.isOnline()) {
                member.sendMessage(comp);
            }
        }
    }
}
