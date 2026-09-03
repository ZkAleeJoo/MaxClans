package org.zkaleejoo.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.managers.ClanManager;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.models.ClanFlag;
import org.zkaleejoo.models.ClanPlayer;
import org.zkaleejoo.models.ClanRole;
import org.zkaleejoo.utils.MessageUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MainCommand implements CommandExecutor, TabCompleter {

    private final OnlyClans plugin;

    public MainCommand(OnlyClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {

        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("onlyclans.command.reload") && !sender.hasPermission("onlyclans.admin")) {
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
            if (!player.hasPermission("onlyclans.command.main")) {
                sendNoPermission(player);
                return true;
            }
            Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
            plugin.getMenuBuilder().openMenu(player, "main", clan);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "help" -> {
                if (!sender.hasPermission("onlyclans.command.help")) {
                    sendNoPermission(sender);
                    return true;
                }
                help(sender);
                return true;
            }
            case "list", "browse" -> {
                if (!player.hasPermission("onlyclans.command.list")) {
                    sendNoPermission(player);
                    return true;
                }
                Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
                plugin.getMenuBuilder().openMenu(player, "clan_list", clan, 0);
                return true;
            }
            case "create" -> {
                if (!player.hasPermission("onlyclans.command.create")) {
                    sendNoPermission(player);
                    return true;
                }
                if (args.length < 2) {
                    plugin.getMenuBuilder().openMenu(player, "create", null);
                } else {
                    String name = args[1];
                    if (name.length() < 3 || name.length() > 16) {
                        player.sendMessage(MessageUtils.getColoredMessage(
                                plugin.getMainConfigManager().getPrefix()
                                        + plugin.getMainConfigManager().getMessage("invalid-length",
                                                "&cClan name must be between 3 and 16 characters.")));
                        return true;
                    }
                    if (!name.matches("^[a-zA-Z0-9_]+$")) {
                        player.sendMessage(MessageUtils.getColoredMessage(
                                plugin.getMainConfigManager().getPrefix()
                                        + plugin.getMainConfigManager().getMessage("invalid-characters",
                                                "&cClan name can only contain letters, numbers, and underscores.")));
                        return true;
                    }
                    String tag = name.substring(0, 3).toUpperCase();
                    clanManager.createClan(player, name, tag);
                }
                return true;
            }
            case "disband" -> {
                if (!player.hasPermission("onlyclans.command.disband")) {
                    sendNoPermission(player);
                    return true;
                }
                Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
                if (clan == null) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                     .getMessage("not-in-clan", "&cYou are not in a clan.")));
                    return true;
                }
                if (!clan.getMember(player.getUniqueId()).isLeader()) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("only-leader-disband", "&cOnly the leader can disband the clan.")));
                    return true;
                }
                plugin.getMenuBuilder().openMenu(player, "confirm-disband", clan);
                return true;
            }
            case "invite" -> {
                if (!player.hasPermission("onlyclans.command.invite")) {
                    sendNoPermission(player);
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("usage-invite", "&cUsage: /clan invite <player>")));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("player-not-found", "&cPlayer not found or offline.")));
                    return true;
                }
                clanManager.invitePlayer(player, target);
                return true;
            }
            case "request", "join" -> {
                if (!player.hasPermission("onlyclans.command.request")) {
                    sendNoPermission(player);
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("usage-request", "&cUsage: /clan request <clan>")));
                    return true;
                }
                String clanName = args[1];
                Clan targetClan = clanManager.getClanByName(clanName);
                if (targetClan == null) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("clan-not-exists", "&cThat clan no longer exists.")));
                    return true;
                }
                clanManager.requestToJoinClan(player, targetClan);
                return true;
            }
            case "acceptrequest", "acceptjoin" -> {
                if (!player.hasPermission("onlyclans.command.acceptrequest")) {
                    sendNoPermission(player);
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("usage-acceptrequest", "&cUsage: /clan acceptrequest <player>")));
                    return true;
                }
                clanManager.acceptJoinRequest(player, args[1]);
                return true;
            }
            case "denyrequest", "denyjoin" -> {
                if (!player.hasPermission("onlyclans.command.denyrequest")) {
                    sendNoPermission(player);
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("usage-denyrequest", "&cUsage: /clan denyrequest <player>")));
                    return true;
                }
                clanManager.denyJoinRequest(player, args[1]);
                return true;
            }
            case "accept" -> {
                if (!player.hasPermission("onlyclans.command.accept")) {
                    sendNoPermission(player);
                    return true;
                }
                clanManager.acceptInvite(player);
                return true;
            }
            case "deny" -> {
                if (!player.hasPermission("onlyclans.command.deny")) {
                    sendNoPermission(player);
                    return true;
                }
                clanManager.denyInvite(player);
                return true;
            }
            case "leave" -> {
                if (!player.hasPermission("onlyclans.command.leave")) {
                    sendNoPermission(player);
                    return true;
                }
                clanManager.leaveClan(player);
                return true;
            }
            case "kick" -> {
                if (!player.hasPermission("onlyclans.command.kick")) {
                    sendNoPermission(player);
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("usage-kick", "&cUsage: /clan kick <player>")));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("player-not-found", "&cPlayer not found or offline.")));
                    return true;
                }
                clanManager.kickPlayer(player, target);
                return true;
            }
            case "promote" -> {
                if (!player.hasPermission("onlyclans.command.promote")) {
                    sendNoPermission(player);
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("usage-promote", "&cUsage: /clan promote <player>")));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("player-not-found", "&cPlayer not found or offline.")));
                    return true;
                }
                clanManager.promotePlayer(player, target);
                return true;
            }
            case "demote" -> {
                if (!player.hasPermission("onlyclans.command.demote")) {
                    sendNoPermission(player);
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("usage-demote", "&cUsage: /clan demote <player>")));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("player-not-found", "&cPlayer not found or offline.")));
                    return true;
                }
                clanManager.demotePlayer(player, target);
                return true;
            }
            case "chat", "c" -> {
                if (!player.hasPermission("onlyclans.command.chat")) {
                    sendNoPermission(player);
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("usage-chat-clan", "&cUsage: /clan chat <message>")));
                    return true;
                }
                Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
                if (clan == null) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("not-in-clan", "&cYou are not in a clan.")));
                    return true;
                }
                String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                clanManager.sendClanMessage(clan, player, message);
                return true;
            }
            case "info" -> {
                if (!player.hasPermission("onlyclans.command.info")) {
                    sendNoPermission(player);
                    return true;
                }
                Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
                if (clan != null) {
                    plugin.getMenuBuilder().openMenu(player, "info", clan);
                } else {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("not-in-clan", "&cYou are not in a clan.")));
                }
                return true;
            }
            case "flags", "settings" -> {
                if (!player.hasPermission("onlyclans.command.flags") && !player.hasPermission("onlyclans.command.settings")) {
                    sendNoPermission(player);
                    return true;
                }
                Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
                if (clan != null) {
                    ClanPlayer cp = clan.getMember(player.getUniqueId());
                    boolean canAccess = player.hasPermission("onlyclans.admin")
                            || (cp != null && cp.hasRoleAtLeast(ClanRole.MODERATOR));
                    if (canAccess) {
                        plugin.getMenuBuilder().openMenu(player, sub.equals("flags") ? "flags" : "settings", clan);
                    } else {
                        player.sendMessage(MessageUtils.getColoredMessage(
                                plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                        .getMessage("no-flag-permission",
                                                "&cOnly clan leaders and moderators can access clan settings.")));
                    }
                } else {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("not-in-clan", "&cYou are not in a clan.")));
                }
                return true;
            }
            case "flag" -> {
                if (!player.hasPermission("onlyclans.command.flags")) {
                    sendNoPermission(player);
                    return true;
                }
                Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
                if (clan == null) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("not-in-clan", "&cYou are not in a clan.")));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("usage-flag", "&cUsage: /clan flag <flag> [on|off|toggle]")));
                    return true;
                }
                ClanFlag flag = ClanFlag.fromKey(args[1]);
                if (flag == null) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("invalid-flag", "&cInvalid flag name. Available: friendly_fire, open_join, ally_damage, member_invites, visible_in_list, public_home, spy_chat")));
                    return true;
                }
                if (args.length >= 3) {
                    String state = args[2].toLowerCase();
                    boolean newVal;
                    if (state.equals("on") || state.equals("true") || state.equals("enable") || state.equals("1")) {
                        newVal = true;
                    } else if (state.equals("off") || state.equals("false") || state.equals("disable") || state.equals("0")) {
                        newVal = false;
                    } else {
                        newVal = !clan.getFlag(flag);
                    }
                    clanManager.setFlag(clan, flag, newVal, player);
                } else {
                    clanManager.toggleFlag(clan, flag, player);
                }
                return true;
            }
            case "spy" -> {
                if (!player.hasPermission("onlyclans.spy") && !player.hasPermission("onlyclans.admin")) {
                    sendNoPermission(player);
                    return true;
                }
                boolean newState;
                if (args.length >= 2) {
                    String state = args[1].toLowerCase();
                    newState = state.equals("on") || state.equals("true") || state.equals("enable") || state.equals("1");
                    clanManager.setSpyMode(player.getUniqueId(), newState);
                } else {
                    newState = clanManager.toggleSpyMode(player.getUniqueId());
                }
                if (newState) {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("spy-enabled", "&aClan chat spy mode enabled.")));
                } else {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("spy-disabled", "&cClan chat spy mode disabled.")));
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
            if (sender.hasPermission("onlyclans.command.create")) completions.add("create");
            if (sender.hasPermission("onlyclans.command.disband")) completions.add("disband");
            if (sender.hasPermission("onlyclans.command.invite")) completions.add("invite");
            if (sender.hasPermission("onlyclans.command.accept")) completions.add("accept");
            if (sender.hasPermission("onlyclans.command.deny")) completions.add("deny");
            if (sender.hasPermission("onlyclans.command.leave")) completions.add("leave");
            if (sender.hasPermission("onlyclans.command.kick")) completions.add("kick");
            if (sender.hasPermission("onlyclans.command.promote")) completions.add("promote");
            if (sender.hasPermission("onlyclans.command.demote")) completions.add("demote");
            if (sender.hasPermission("onlyclans.command.chat")) completions.add("chat");
            if (sender.hasPermission("onlyclans.command.info")) completions.add("info");
            if (sender.hasPermission("onlyclans.command.list")) completions.add("list");
            if (sender.hasPermission("onlyclans.command.flags")) {
                completions.add("flags");
                completions.add("flag");
            }
            if (sender.hasPermission("onlyclans.command.settings")) completions.add("settings");
            if (sender.hasPermission("onlyclans.spy") || sender.hasPermission("onlyclans.admin")) completions.add("spy");
            if (sender.hasPermission("onlyclans.command.request")) completions.add("request");
            if (sender.hasPermission("onlyclans.command.acceptrequest")) completions.add("acceptrequest");
            if (sender.hasPermission("onlyclans.command.denyrequest")) completions.add("denyrequest");
            if (sender.hasPermission("onlyclans.command.help")) completions.add("help");
            if (sender.hasPermission("onlyclans.command.reload") || sender.hasPermission("onlyclans.admin")) {
                completions.add("reload");
            }
            return filterCompletions(completions, args[0]);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("flag") && sender.hasPermission("onlyclans.command.flags")) {
                for (ClanFlag f : ClanFlag.values()) {
                    completions.add(f.getKey());
                }
                return filterCompletions(completions, args[1]);
            }
            if (sub.equals("spy") && (sender.hasPermission("onlyclans.spy") || sender.hasPermission("onlyclans.admin"))) {
                completions.add("on");
                completions.add("off");
                return filterCompletions(completions, args[1]);
            }
            if (sub.equals("invite") && sender.hasPermission("onlyclans.command.invite")
                    || sub.equals("kick") && sender.hasPermission("onlyclans.command.kick")
                    || sub.equals("promote") && sender.hasPermission("onlyclans.command.promote")
                    || sub.equals("demote") && sender.hasPermission("onlyclans.command.demote")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    completions.add(p.getName());
                }
                return filterCompletions(completions, args[1]);
            }
            if ((sub.equals("request") || sub.equals("join")) && sender.hasPermission("onlyclans.command.request")) {
                for (Clan c : plugin.getClanManager().getAllClans()) {
                    completions.add(c.getName());
                }
                return filterCompletions(completions, args[1]);
            }
            if ((sub.equals("acceptrequest") || sub.equals("acceptjoin")) && sender.hasPermission("onlyclans.command.acceptrequest")
                    || (sub.equals("denyrequest") || sub.equals("denyjoin")) && sender.hasPermission("onlyclans.command.denyrequest")) {
                if (sender instanceof Player p) {
                    Clan clan = plugin.getClanManager().getClanByPlayer(p.getUniqueId());
                    if (clan != null) {
                        for (UUID reqUuid : clan.getJoinRequests()) {
                            Player targetP = Bukkit.getPlayer(reqUuid);
                            if (targetP != null) {
                                completions.add(targetP.getName());
                            } else {
                                OfflinePlayer op = Bukkit.getOfflinePlayer(reqUuid);
                                if (op.getName() != null) {
                                    completions.add(op.getName());
                                }
                            }
                        }
                    }
                }
                return filterCompletions(completions, args[1]);
            }
        }

        if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("flag") && sender.hasPermission("onlyclans.command.flags")) {
                completions.add("on");
                completions.add("off");
                completions.add("toggle");
                return filterCompletions(completions, args[2]);
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
