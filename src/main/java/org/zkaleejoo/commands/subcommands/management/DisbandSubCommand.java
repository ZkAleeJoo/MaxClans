package org.zkaleejoo.commands.subcommands.management;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.utils.MessageUtils;

public class DisbandSubCommand extends SubCommand {

    public DisbandSubCommand(OnlyClans plugin) {
        super(plugin, "disband");
    }

    @Override
    public String getPermission() {
        return "onlyclans.command.disband";
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

        if (!clan.getMember(player.getUniqueId()).isLeader()) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("only-leader-disband", "&cOnly the leader can disband the clan.")));
            return;
        }

        plugin.getMenuBuilder().openMenu(player, "confirm-disband", clan);
    }
}
