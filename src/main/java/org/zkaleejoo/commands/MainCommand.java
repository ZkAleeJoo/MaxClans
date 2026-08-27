package org.zkaleejoo.commands;

import org.bukkit.Bukkit;
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
        ClanManager clanManager = plugin.getClanManager();

        if (args.length == 0) {
            Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
            plugin.getMenuBuilder().openMenu(player, "main", clan);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "help" -> {
                help(sender);
                return true;
            }
            case "create" -> {
                if (args.length < 2) {
                    plugin.getMenuBuilder().openMenu(player, "create", null);
                } else {
                    String name = args[1];
                    if (name.length() < 3 || name.length() > 16) {
                        player.sendMessage(MessageUtils.getColoredMessage(
                                plugin.getMainConfigManager().getPrefix()
                                        + plugin.getMainConfigManager().getMessage("invalid-name-length", "&cClan name must be between 3 and 16 characters.")));
                        return true;
                    }
                    if (!name.matches("^[a-zA-Z0-9_]+$")) {
                        player.sendMessage(MessageUtils.getColoredMessage(
                                plugin.getMainConfigManager().getPrefix()
                                        + plugin.getMainConfigManager().getMessage("invalid-name-format", "&cClan name can only contain letters, numbers, and underscores.")));
                        return true;
                    }
                    String tag = name.substring(0, 3).toUpperCase();
                    clanManager.createClan(player, name, tag);
                }
                return true;
            }
            case "disband" -> {
                Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
                if (clan == null) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getMessage("not-in-clan", "&cYou are not in a clan.")));
                    return true;
                }
                if (!clan.getMember(player.getUniqueId()).isLeader()) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getMessage("only-leader-disband", "&cOnly the leader can disband the clan.")));
                    return true;
                }
                plugin.getMenuBuilder().openMenu(player, "confirm-disband", clan);
                return true;
            }
            case "invite" -> {
                if (args.length < 2) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getMessage("usage-invite", "&cUsage: /clan invite <player>")));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getMessage("player-not-found", "&cPlayer not found or offline.")));
                    return true;
                }
                clanManager.invitePlayer(player, target);
                return true;
            }
            case "accept" -> {
                clanManager.acceptInvite(player);
                return true;
            }
            case "deny" -> {
                clanManager.denyInvite(player);
                return true;
            }
            case "leave" -> {
                clanManager.leaveClan(player);
                return true;
            }
            case "kick" -> {
                if (args.length < 2) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getMessage("usage-kick", "&cUsage: /clan kick <player>")));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getMessage("player-not-found", "&cPlayer not found or offline.")));
                    return true;
                }
                clanManager.kickPlayer(player, target);
                return true;
            }
            case "promote" -> {
                if (args.length < 2) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getMessage("usage-promote", "&cUsage: /clan promote <player>")));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getMessage("player-not-found", "&cPlayer not found or offline.")));
                    return true;
                }
                clanManager.promotePlayer(player, target);
                return true;
            }
            case "demote" -> {
                if (args.length < 2) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getMessage("usage-demote", "&cUsage: /clan demote <player>")));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getMessage("player-not-found", "&cPlayer not found or offline.")));
                    return true;
                }
                clanManager.demotePlayer(player, target);
                return true;
            }
            case "chat", "c" -> {
                if (args.length < 2) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getMessage("usage-chat", "&cUsage: /clan chat <message>")));
                    return true;
                }
                Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
                if (clan == null) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getMessage("not-in-clan", "&cYou are not in a clan.")));
                    return true;
                }
                String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                clanManager.sendClanMessage(clan, player, message);
                return true;
            }
            case "info" -> {
                Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
                if (clan != null) {
                    plugin.getMenuBuilder().openMenu(player, "info", clan);
                } else {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getMessage("not-in-clan", "&cYou are not in a clan.")));
                }
                return true;
            }
            default -> {
                sender.sendMessage(MessageUtils.getColoredMessage(
                        plugin.getMainConfigManager().getPrefix()
                                + plugin.getMainConfigManager().getSubcommandInvalid()));
                return true;
            }
        }
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
            completions.addAll(Arrays.asList(
                    "create", "disband", "invite", "accept", "deny",
                    "leave", "kick", "promote", "demote", "chat", "info", "help"));
            if (sender.hasPermission("onlyclans.admin")) {
                completions.add("reload");
            }
            return filterCompletions(completions, args[0]);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("invite") || sub.equals("kick") || sub.equals("promote") || sub.equals("demote")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    completions.add(p.getName());
                }
                return filterCompletions(completions, args[1]);
            }
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
