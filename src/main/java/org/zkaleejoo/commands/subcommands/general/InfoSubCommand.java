package org.zkaleejoo.commands.subcommands.general;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.MaxClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.utils.MessageUtils;

public class InfoSubCommand extends SubCommand {

    public InfoSubCommand(MaxClans plugin) {
        super(plugin, "info");
    }

    @Override
    public String getPermission() {
        return "maxclans.command.info";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan != null) {
            plugin.getMenuBuilder().openMenu(player, "info", clan);
        } else {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("not-in-clan", "&cYou are not in a clan.")));
        }
    }
}
