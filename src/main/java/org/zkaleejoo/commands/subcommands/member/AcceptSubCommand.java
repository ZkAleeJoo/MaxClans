package org.zkaleejoo.commands.subcommands.member;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.MaxClans;
import org.zkaleejoo.commands.SubCommand;

public class AcceptSubCommand extends SubCommand {

    public AcceptSubCommand(MaxClans plugin) {
        super(plugin, "accept");
    }

    @Override
    public String getPermission() {
        return "maxclans.command.accept";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        plugin.getClanManager().acceptInvite(player);
    }
}
