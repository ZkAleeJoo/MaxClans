package org.zkaleejoo.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.utils.MessageUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainCommand implements CommandExecutor, TabCompleter {

    private final OnlyClans plugin;

    public MainCommand(OnlyClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {

        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("onlyclans.admin")) {
                sendNoPermission(sender);
                return true;
            }
            plugin.reloadPluginState();
            sender.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getPluginReload()));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getMsgConsole()));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            if (!player.hasPermission("onlyclans.admin")) {
                sendNoPermission(player);
                return true;
            }
            help(sender);
            return true;
        }

        sender.sendMessage(MessageUtils.getColoredMessage(
                plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getSubcommandInvalid()));
        return true;
    }

    private void sendNoPermission(CommandSender sender) {
        sender.sendMessage(MessageUtils.getColoredMessage(
                plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getNoPermission()));
    }

    public void help(CommandSender sender) {
        String titleTemplate = plugin.getMainConfigManager().getHelpTitle();
        sender.sendMessage(MessageUtils.getColoredMessage(plugin.getMainConfigManager().getPrefix()
                + titleTemplate.replace("{version}", plugin.getPluginMeta().getVersion())));

        List<String> helpLines = plugin.getMainConfigManager().getHelpLines();
        for (String line : helpLines) {
            sender.sendMessage(MessageUtils.getColoredMessage(line));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("onlyclans.admin")) {
                completions.addAll(Arrays.asList("reload", "help"));
            }
            return filterCompletions(completions, args[0]);
        }

        return completions;
    }

    private List<String> filterCompletions(List<String> completions, String input) {
        List<String> filtered = new ArrayList<>();
        for (String completion : completions) {
            if (completion.toLowerCase().startsWith(input.toLowerCase())) {
                filtered.add(completion);
            }
        }
        return filtered;
    }
}
