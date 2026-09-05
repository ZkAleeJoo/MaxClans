package org.zkaleejoo.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.managers.ClanManager;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class ChatCommand implements CommandExecutor, TabCompleter {

    private final OnlyClans plugin;

    public ChatCommand(OnlyClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getMsgConsole()));
            return true;
        }

        if (!player.hasPermission("onlyclans.command.chat")) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getNoPermission()));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getMessage("usage-chat", "&cUsage: /c <message>")));
            return true;
        }

        ClanManager clanManager = plugin.getClanManager();
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());

        if (clan == null) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getMessage("not-in-clan", "&cYou are not in a clan.")));
            return true;
        }

        String message = String.join(" ", args);
        clanManager.sendClanMessage(clan, player, message);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
