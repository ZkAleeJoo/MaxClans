package org.zkaleejoo.commands.subcommands.admin;

import org.bukkit.command.CommandSender;
import org.zkaleejoo.MaxClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class ExpAdminSubCommand extends SubCommand {

    public ExpAdminSubCommand(MaxClans plugin) {
        super(plugin, "admin", "a");
    }

    @Override
    public String getPermission() {
        return "maxclans.admin";
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("usage-admin", "&cUsage: /clan admin <addexp|setlevel|resetexp> <clan> <amount>")));
            return;
        }

        String action = args[1].toLowerCase();
        String clanName = args[2];
        Clan clan = plugin.getClanManager().getClanByName(clanName);

        if (clan == null) {
            sender.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("clan-not-exists", "&cThat clan no longer exists.")));
            return;
        }

        switch (action) {
            case "addexp" -> {
                int amount;
                try {
                    amount = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(MessageUtils.toComponent(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("admin-invalid-amount", "&cInvalid amount.")));
                    return;
                }
                plugin.getClanLevelManager().addExp(clan, amount, "Admin", null);
                sender.sendMessage(MessageUtils.toComponent(
                        plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                .getMessage("admin-exp-added", "&aAdded &e{amount}&a Clan EXP to clan &f{clan}&a.")
                                .replace("{amount}", String.valueOf(amount))
                                .replace("{clan}", clan.getName())));
            }
            case "setlevel" -> {
                int lvl;
                try {
                    lvl = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(MessageUtils.toComponent(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("admin-invalid-level", "&cInvalid level.")));
                    return;
                }
                clan.setLevel(lvl);
                clan.setExp(0);
                plugin.getClanStorage().updateClan(clan);
                sender.sendMessage(MessageUtils.toComponent(
                        plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                .getMessage("admin-level-set", "&aClan &f{clan}&a level has been set to &e{level}&a.")
                                .replace("{clan}", clan.getName())
                                .replace("{level}", String.valueOf(lvl))));
            }
            case "resetexp" -> {
                clan.setExp(0);
                plugin.getClanStorage().updateClan(clan);
                sender.sendMessage(MessageUtils.toComponent(
                        plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                .getMessage("admin-exp-reset", "&aClan &f{clan}&a EXP has been reset.")
                                .replace("{clan}", clan.getName())));
            }
            default -> sender.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("admin-unknown-action", "&cUnknown action. Use addexp, setlevel, or resetexp.")));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return filterCompletions(List.of("addexp", "setlevel", "resetexp"), args[1]);
        }
        if (args.length == 3) {
            List<String> list = new ArrayList<>();
            for (Clan c : plugin.getClanManager().getAllClans()) {
                list.add(c.getName());
            }
            return filterCompletions(list, args[2]);
        }
        return super.tabComplete(sender, args);
    }
}
