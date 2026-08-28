package org.zkaleejoo.managers;

import org.bukkit.Bukkit;
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
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getMessage("clan-exists",
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
                            .getMessage("player-already-in-clan", "&cThat player is already in a clan.")));
            return;
        }

        if (pendingInvites.containsKey(target.getUniqueId())) {
            inviter.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("player-already-invited", "&cThat player already has a pending invite.")));
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
                                "&aYou have been invited to join &f{clan}&a.")
                                .replace("{clan}", clan.getName())));
                                
        net.kyori.adventure.text.Component acceptButton = net.kyori.adventure.text.Component.text(" [ACCEPT] ")
                .color(net.kyori.adventure.text.format.NamedTextColor.GREEN)
                .decorate(net.kyori.adventure.text.format.TextDecoration.BOLD)
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/clan accept"))
                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(net.kyori.adventure.text.Component.text("Click to join " + clan.getName())));

        net.kyori.adventure.text.Component denyButton = net.kyori.adventure.text.Component.text(" [DENY] ")
                .color(net.kyori.adventure.text.format.NamedTextColor.RED)
                .decorate(net.kyori.adventure.text.format.TextDecoration.BOLD)
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/clan deny"))
                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(net.kyori.adventure.text.Component.text("Click to decline")));

        target.sendMessage(net.kyori.adventure.text.Component.text("   ")
                .append(acceptButton)
                .append(net.kyori.adventure.text.Component.text("   "))
                .append(denyButton));
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
                            .getMessage("clan-does-not-exist", "&cThat clan no longer exists.")));
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
                                .getMessage("clan-joined", "&aYou have joined the clan '&f{clan}&a'!")
                                .replace("{clan}", clanName)));

        broadcastToClan(clan, plugin.getMainConfigManager().getPrefix()
                + plugin.getMainConfigManager().getMessage("clan-member-joined", "&a{player} has joined the clan!")
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
                                .getMessage("clan-left", "&eYou have left the clan '&f{clan}&e'.")
                                .replace("{clan}", clan.getName())));

        broadcastToClan(clan, plugin.getMainConfigManager().getPrefix()
                + plugin.getMainConfigManager().getMessage("clan-member-left", "&e{player} has left the clan.")
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
                        .getMessage("clan-member-kicked", "&c{player} has been kicked from the clan.")
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
                            .getMessage("only-leader-promote", "&cOnly the leader can promote members.")));
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
                                .getMessage("player-promoted", "&a{player} has been promoted to Moderator.")
                                .replace("{player}", target.getName())));
        target.sendMessage(MessageUtils.getColoredMessage(
                plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getMessage("promoted",
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
                            .getMessage("only-leader-demote", "&cOnly the leader can demote members.")));
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
                                .getMessage("player-demoted", "&e{player} has been demoted to Member.")
                                .replace("{player}", target.getName())));
        target.sendMessage(MessageUtils.getColoredMessage(
                plugin.getMainConfigManager().getPrefix()
                        + plugin.getMainConfigManager().getMessage("demoted", "&cYou have been demoted to Member.")));
    }

    public void toggleFriendlyFire(Clan clan) {
        clan.setFriendlyFire(!clan.isFriendlyFire());
        storage.updateClan(clan);

        String status = clan.isFriendlyFire() ? "&aON" : "&cOFF";
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
