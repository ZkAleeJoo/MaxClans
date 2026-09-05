package org.zkaleejoo.commands.subcommands.member;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.commands.SubCommand;

public class LeaveSubCommand extends SubCommand {

    public LeaveSubCommand(OnlyClans plugin) {
        super(plugin, "leave");
    }

    @Override
    public String getPermission() {
        return "onlyclans.command.leave";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        plugin.getClanManager().leaveClan(player);
    }
}
