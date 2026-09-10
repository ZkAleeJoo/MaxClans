package org.zkaleejoo.commands.subcommands.general;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.MaxClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.models.Clan;

public class ListSubCommand extends SubCommand {

    public ListSubCommand(MaxClans plugin) {
        super(plugin, "list", "browse");
    }

    @Override
    public String getPermission() {
        return "maxclans.command.list";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        plugin.getMenuBuilder().openMenu(player, "clan_list", clan, 0);
    }
}
