package org.zkaleejoo.commands.subcommands.member;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.MaxClans;
import org.zkaleejoo.commands.SubCommand;

public class DenySubCommand extends SubCommand {

    public DenySubCommand(MaxClans plugin) {
        super(plugin, "deny");
    }

    @Override
    public String getPermission() {
        return "maxclans.command.deny";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        plugin.getClanManager().denyInvite(player);
    }
}
