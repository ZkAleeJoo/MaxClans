package org.zkaleejoo.commands.subcommands.management;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.models.ClanHome;
import org.zkaleejoo.models.ClanPlayer;
import org.zkaleejoo.models.ClanRole;
import org.zkaleejoo.utils.MessageUtils;
import org.zkaleejoo.utils.SoundUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DelHomeSubCommand extends SubCommand {

    public DelHomeSubCommand(OnlyClans plugin) {
        super(plugin, "delhome", "deletehome", "rmhome");
    }

    @Override
    public String getPermission() {
        return "onlyclans.command.delhome";
    }

    @Override
    public List<String> getTabSuggestions(CommandSender sender) {
        if (!hasPermission(sender)) {
            return Collections.emptyList();
        }
        return List.of("delhome");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() +
                            plugin.getMainConfigManager().getMessage("not-in-clan", "&cYou are not in a clan.")));
            SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
            return;
        }

        ClanPlayer cp = clan.getMember(player.getUniqueId());
        if (cp == null || !cp.hasRoleAtLeast(ClanRole.MODERATOR)) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() +
                            plugin.getMainConfigManager().getMessage("home-no-permission",
                                    "&cOnly clan leaders and moderators can delete clan homes.")));
            SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
            return;
        }

        if (clan.getHomeCount() == 0) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() +
                            plugin.getMainConfigManager().getMessage("home-none-set",
                                    "&cYour clan does not have any homes set.")));
            SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
            return;
        }

        ClanHome targetHome = null;

        if (args.length < 2) {
            if (clan.getHomeCount() == 1) {
                targetHome = clan.getHomes().values().iterator().next();
            } else if (clan.hasHome("default")) {
                targetHome = clan.getHome("default");
            } else {
                String homesList = String.join(", ", clan.getHomeNames());
                player.sendMessage(MessageUtils.toComponent(
                        plugin.getMainConfigManager().getPrefix() +
                                plugin.getMainConfigManager().getMessage("usage-delhome",
                                        "&cUsage: /clan delhome <name> (Available: {homes})")
                                        .replace("{homes}", homesList)));
                SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                return;
            }
        } else {
            String name = args[1].trim();
            targetHome = clan.getHome(name);
            if (targetHome == null) {
                player.sendMessage(MessageUtils.toComponent(
                        plugin.getMainConfigManager().getPrefix() +
                                plugin.getMainConfigManager().getMessage("home-not-found",
                                        "&cClan home '&f{home}&c' does not exist.")
                                        .replace("{home}", name)));
                SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                return;
            }
        }

        clan.removeHome(targetHome.getName());
        plugin.getClanStorage().deleteHome(clan.getName(), targetHome.getName());

        player.sendMessage(MessageUtils.toComponent(
                plugin.getMainConfigManager().getPrefix() +
                        plugin.getMainConfigManager().getMessage("home-deleted-success",
                                "&aClan home '&f{home}&a' has been deleted.")
                                .replace("{home}", targetHome.getName())));
        SoundUtils.playSound(player, "BLOCK_NOTE_BLOCK_PLING", 1.0f, 1.2f);

        String broadcastMsg = plugin.getMainConfigManager().getMessage("home-deleted-broadcast",
                "&eClan home '&f{home}&e' was deleted by &f{player}&e.")
                .replace("{home}", targetHome.getName())
                .replace("{player}", player.getName());

        for (UUID memberUuid : clan.getMembers().keySet()) {
            if (memberUuid.equals(player.getUniqueId()))
                continue;
            Player member = Bukkit.getPlayer(memberUuid);
            if (member != null && member.isOnline()) {
                member.sendMessage(MessageUtils.toComponent(
                        plugin.getMainConfigManager().getPrefix() + broadcastMsg));
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2 && sender instanceof Player player) {
            Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
            if (clan != null) {
                return filterCompletions(new ArrayList<>(clan.getHomeNames()), args[1]);
            }
        }
        return Collections.emptyList();
    }
}
