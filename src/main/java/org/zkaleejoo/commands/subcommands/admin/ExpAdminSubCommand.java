package org.zkaleejoo.commands.subcommands.admin;

import org.bukkit.command.CommandSender;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class ExpAdminSubCommand extends SubCommand {

    public ExpAdminSubCommand(OnlyClans plugin) {
        super(plugin, "admin", "a");
    }

    @Override
    public String getPermission() {
        return "onlyclans.admin";
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + "&cUso: /clan admin <addexp|setlevel|resetexp> <clan> <cantidad>"));
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
                    sender.sendMessage(MessageUtils.toComponent(plugin.getMainConfigManager().getPrefix() + "&cCantidad inválida."));
                    return;
                }
                plugin.getClanLevelManager().addExp(clan, amount, "Admin", null);
                sender.sendMessage(MessageUtils.toComponent(
                        plugin.getMainConfigManager().getPrefix() + "&aSe añadieron &e" + amount + " Clan EXP &aal clan &f" + clan.getName()));
            }
            case "setlevel" -> {
                int lvl;
                try {
                    lvl = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(MessageUtils.toComponent(plugin.getMainConfigManager().getPrefix() + "&cNivel inválido."));
                    return;
                }
                clan.setLevel(lvl);
                clan.setExp(0);
                plugin.getClanStorage().updateClan(clan);
                sender.sendMessage(MessageUtils.toComponent(
                        plugin.getMainConfigManager().getPrefix() + "&aEl nivel del clan &f" + clan.getName() + " &aha sido fijado en &e" + lvl));
            }
            case "resetexp" -> {
                clan.setExp(0);
                plugin.getClanStorage().updateClan(clan);
                sender.sendMessage(MessageUtils.toComponent(
                        plugin.getMainConfigManager().getPrefix() + "&aSe ha reiniciado la EXP del clan &f" + clan.getName()));
            }
            default -> sender.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + "&cAcción desconocida. Usa addexp, setlevel o resetexp."));
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
