package org.zkaleejoo.commands.subcommands.management;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.MaxClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class AllySubCommand extends SubCommand {

    public AllySubCommand(MaxClans plugin) {
        super(plugin, "ally", "aliado", "aliados");
    }

    @Override
    public String getPermission() {
        return "maxclans.command.ally";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("not-in-clan", "&cYou are not in a clan.")));
            return;
        }

        if (args.length < 2 || args[1].equalsIgnoreCase("list") || args[1].equalsIgnoreCase("lista")) {
            showAllyList(player, clan);
            return;
        }

        String action = args[1].toLowerCase();

        if (args.length < 3) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("usage-ally", "&cUsage: /clan ally <add|accept|deny|remove|list> [clan]")));
            return;
        }

        String targetClanName = args[2];
        Clan targetClan = plugin.getClanManager().getClanByName(targetClanName);
        if (targetClan == null) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("clan-not-exists", "&cThat clan no longer exists.")));
            return;
        }

        switch (action) {
            case "add", "invitar", "proponer" -> plugin.getClanManager().requestAlly(player, targetClan);
            case "accept", "aceptar" -> plugin.getClanManager().acceptAlly(player, targetClan);
            case "deny", "rechazar" -> plugin.getClanManager().denyAlly(player, targetClan);
            case "remove", "eliminar", "romper" -> plugin.getClanManager().removeAlly(player, targetClan);
            default -> player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("usage-ally", "&cUsage: /clan ally <add|accept|deny|remove|list> [clan]")));
        }
    }

    private void showAllyList(Player player, Clan clan) {
        int maxAllies = plugin.getClanLevelManager() != null
                ? plugin.getClanLevelManager().getMaxAllies(clan.getLevel())
                : 0;
        player.sendMessage(MessageUtils.toComponent(
                plugin.getMainConfigManager().getMessage("ally-list-header",
                        "&#94A3B8&m━━━━━━━━━━━━━&r &#7DD3FC&lCLAN ALLIANCES &#94A3B8&m━━━━━━━━━━━━━")));
        player.sendMessage(MessageUtils.toComponent(
                plugin.getMainConfigManager().getMessage("ally-list-capacity",
                        " &7Ally capacity: &#6EE7B7{current} &7/ &#6EE7B7{max}")
                        .replace("{current}", String.valueOf(clan.getAllies().size()))
                        .replace("{max}", String.valueOf(maxAllies))));

        if (clan.getAllies().isEmpty()) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getMessage("ally-list-empty",
                            " &7Your clan does not have any allied clans at this time.")));
            if (maxAllies <= 0) {
                player.sendMessage(MessageUtils.toComponent(
                        plugin.getMainConfigManager().getMessage("ally-list-unlock-hint",
                                " &#FDE047&o(Unlock Clan Level 4 to form your first alliance)")));
            }
        } else {
            for (String allyName : clan.getAllies()) {
                Clan ally = plugin.getClanManager().getClanByName(allyName);
                String tag = (ally != null) ? " &#94A3B8[&#FDE047" + ally.getTag() + "&#94A3B8]" : "";
                player.sendMessage(MessageUtils.toComponent(" &#7DD3FC✦ &f" + allyName + tag));
            }
        }
        player.sendMessage(MessageUtils.toComponent(
                plugin.getMainConfigManager().getMessage("ally-list-footer",
                        "&#94A3B8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return filterCompletions(List.of("add", "accept", "deny", "remove", "list"), args[1]);
        }
        if (args.length == 3) {
            List<String> clanNames = new ArrayList<>();
            for (Clan c : plugin.getClanManager().getAllClans()) {
                clanNames.add(c.getName());
            }
            return filterCompletions(clanNames, args[2]);
        }
        return super.tabComplete(sender, args);
    }
}
