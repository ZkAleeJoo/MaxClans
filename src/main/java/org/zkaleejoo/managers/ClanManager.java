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
import org.zkaleejoo.models.ClanPlayer;
import org.zkaleejoo.models.ClanRole;
import org.zkaleejoo.utils.MessageUtils;

import java.util.*;

public class ClanManager {

    private final OnlyClans plugin;
    private final ClanStorage storage;

    private final Map<String, Clan> clans = new HashMap<>();
    private final Map<UUID, String> playerClanMap = new HashMap<>();
    private final Set<UUID> pendingCreations = new HashSet<>();
    private final Map<UUID, String> pendingInvites = new HashMap<>();

    public ClanManager(OnlyClans plugin, ClanStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
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
        if (cp == null || !cp.hasRoleAtLeast(ClanRole.MODERATOR)) {
            inviter.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("no-invite-permission", "&cYou don't have permission to invite players.")));
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
        String acceptHoverText = plugin.getMainConfigManager().getMessage("invite-hover-accept", "&aClick to join &f{clan}")
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
                            + plugin.getMainConfigManager().getMessage("clan-not-exists", "&cThat clan no longer exists.")));
            return;
        }

        if (isInClan(applicant.getUniqueId())) {
            applicant.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("already-in-clan", "&cYou are already in a clan.")));
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
                            .getMessage("request-no-staff-online", "&cCannot send request: No leader or moderator of '&f{clan}&c' is online.")
                            .replace("{clan}", clan.getName())));
            return;
        }

        clan.addJoinRequest(applicant.getUniqueId());

        applicant.sendMessage(MessageUtils.getColoredMessage(
                plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                        .getMessage("request-sent", "&aJoin request sent to '&f{clan}&a'! Waiting for a leader or moderator to accept.")
                        .replace("{clan}", clan.getName())));

        String acceptBtnText = plugin.getMainConfigManager().getMessage("request-button-accept", " [ACCEPT] ");
        String denyBtnText = plugin.getMainConfigManager().getMessage("request-button-deny", " [DENY] ");
        String acceptHoverText = plugin.getMainConfigManager().getMessage("request-hover-accept", "&aClick to accept &f{player}")
                .replace("{player}", applicant.getName());
        String denyHoverText = plugin.getMainConfigManager().getMessage("request-hover-deny", "&cClick to deny &f{player}")
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
                        + plugin.getMainConfigManager().getMessage("demoted-self", "&cYou have been demoted to Member.")));
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

    public void sendClanMessage(Clan clan, Player sender, String message) {
        String formatted = plugin.getMainConfigManager().getPrefix()
                + plugin.getMainConfigManager()
                        .getMessage("clan-chat-format", "&b[Clan Chat] &f{player}&7: &f{message}")
                        .replace("{player}", sender.getName()).replace("{message}", message);

        for (UUID uuid : clan.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(uuid);
            if (member != null && member.isOnline()) {
                member.sendMessage(MessageUtils.getColoredMessage(formatted));
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

    private void broadcastToClan(Clan clan, String message) {
        for (UUID uuid : clan.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(uuid);
            if (member != null && member.isOnline()) {
                member.sendMessage(MessageUtils.getColoredMessage(message));
            }
        }
    }
}
