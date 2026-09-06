package org.zkaleejoo.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.commands.subcommands.admin.ReloadSubCommand;
import org.zkaleejoo.commands.subcommands.admin.SpySubCommand;
import org.zkaleejoo.commands.subcommands.general.ChatSubCommand;
import org.zkaleejoo.commands.subcommands.general.HelpSubCommand;
import org.zkaleejoo.commands.subcommands.general.HomeSubCommand;
import org.zkaleejoo.commands.subcommands.general.HomesSubCommand;
import org.zkaleejoo.commands.subcommands.general.InfoSubCommand;
import org.zkaleejoo.commands.subcommands.general.ListSubCommand;
import org.zkaleejoo.commands.subcommands.general.TopSubCommand;
import org.zkaleejoo.commands.subcommands.management.CreateSubCommand;
import org.zkaleejoo.commands.subcommands.management.DecorSubCommand;
import org.zkaleejoo.commands.subcommands.management.DelHomeSubCommand;
import org.zkaleejoo.commands.subcommands.management.DisbandSubCommand;
import org.zkaleejoo.commands.subcommands.management.FlagSubCommand;
import org.zkaleejoo.commands.subcommands.management.FlagsSubCommand;
import org.zkaleejoo.commands.subcommands.management.SetHomeSubCommand;
import org.zkaleejoo.commands.subcommands.management.TagSubCommand;
import org.zkaleejoo.commands.subcommands.member.AcceptRequestSubCommand;
import org.zkaleejoo.commands.subcommands.member.AcceptSubCommand;
import org.zkaleejoo.commands.subcommands.member.DemoteSubCommand;
import org.zkaleejoo.commands.subcommands.member.DenyRequestSubCommand;
import org.zkaleejoo.commands.subcommands.member.DenySubCommand;
import org.zkaleejoo.commands.subcommands.member.InviteSubCommand;
import org.zkaleejoo.commands.subcommands.member.KickSubCommand;
import org.zkaleejoo.commands.subcommands.member.LeaveSubCommand;
import org.zkaleejoo.commands.subcommands.member.PromoteSubCommand;
import org.zkaleejoo.commands.subcommands.member.RequestSubCommand;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.utils.MessageUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainCommand implements CommandExecutor, TabCompleter {

    private final OnlyClans plugin;
    private final List<SubCommand> subCommands = new ArrayList<>();
    private final HelpSubCommand helpSubCommand;

    public MainCommand(OnlyClans plugin) {
        this.plugin = plugin;
        this.helpSubCommand = new HelpSubCommand(plugin);

        registerSubCommands();
    }

    private void registerSubCommands() {
        // Admin
        subCommands.add(new ReloadSubCommand(plugin));
        subCommands.add(new SpySubCommand(plugin));
        subCommands.add(new org.zkaleejoo.commands.subcommands.admin.ExpAdminSubCommand(plugin));

        // General
        subCommands.add(helpSubCommand);
        subCommands.add(new ListSubCommand(plugin));
        subCommands.add(new TopSubCommand(plugin));
        subCommands.add(new ChatSubCommand(plugin));
        subCommands.add(new InfoSubCommand(plugin));
        subCommands.add(new HomeSubCommand(plugin));
        subCommands.add(new HomesSubCommand(plugin));
        subCommands.add(new org.zkaleejoo.commands.subcommands.general.LevelSubCommand(plugin));
        subCommands.add(new org.zkaleejoo.commands.subcommands.general.ChestSubCommand(plugin));
        subCommands.add(new org.zkaleejoo.commands.subcommands.general.BankSubCommand(plugin));
        subCommands.add(new org.zkaleejoo.commands.subcommands.general.QuestsSubCommand(plugin));

        // Management
        subCommands.add(new CreateSubCommand(plugin));
        subCommands.add(new DisbandSubCommand(plugin));
        subCommands.add(new DecorSubCommand(plugin));
        subCommands.add(new FlagSubCommand(plugin));
        subCommands.add(new FlagsSubCommand(plugin));
        subCommands.add(new TagSubCommand(plugin));
        subCommands.add(new SetHomeSubCommand(plugin));
        subCommands.add(new DelHomeSubCommand(plugin));
        subCommands.add(new org.zkaleejoo.commands.subcommands.management.AllySubCommand(plugin));

        // Member
        subCommands.add(new InviteSubCommand(plugin));
        subCommands.add(new KickSubCommand(plugin));
        subCommands.add(new PromoteSubCommand(plugin));
        subCommands.add(new DemoteSubCommand(plugin));
        subCommands.add(new LeaveSubCommand(plugin));
        subCommands.add(new AcceptSubCommand(plugin));
        subCommands.add(new DenySubCommand(plugin));
        subCommands.add(new RequestSubCommand(plugin));
        subCommands.add(new AcceptRequestSubCommand(plugin));
        subCommands.add(new DenyRequestSubCommand(plugin));
    }

    public List<SubCommand> getSubCommands() {
        return Collections.unmodifiableList(subCommands);
    }

    public SubCommand getSubCommand(String name) {
        for (SubCommand subCommand : subCommands) {
            if (subCommand.matches(name)) {
                return subCommand;
            }
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(MessageUtils.toComponent(
                        plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getMsgConsole()));
                return true;
            }

            if (!player.hasPermission("onlyclans.command.main")) {
                sendNoPermission(player);
                return true;
            }

            Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
            plugin.getMenuBuilder().openMenu(player, "main", clan);
            return true;
        }

        SubCommand subCommand = getSubCommand(args[0]);
        if (subCommand == null) {
            sender.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix()
                            + plugin.getMainConfigManager().getSubcommandInvalid()));
            return true;
        }

        if (!subCommand.hasPermission(sender)) {
            sendNoPermission(sender);
            return true;
        }

        if (subCommand.isPlayerOnly() && !(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getMsgConsole()));
            return true;
        }

        subCommand.execute(sender, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            for (SubCommand subCommand : subCommands) {
                completions.addAll(subCommand.getTabSuggestions(sender));
            }
            return filterCompletions(completions, args[0]);
        }

        if (args.length >= 2) {
            SubCommand subCommand = getSubCommand(args[0]);
            if (subCommand != null && subCommand.hasPermission(sender)) {
                return subCommand.tabComplete(sender, args);
            }
        }

        return Collections.emptyList();
    }

    public void help(CommandSender sender) {
        helpSubCommand.execute(sender, new String[]{"help"});
    }

    private void sendNoPermission(CommandSender sender) {
        sender.sendMessage(MessageUtils.toComponent(
                plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getNoPermission()));
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
