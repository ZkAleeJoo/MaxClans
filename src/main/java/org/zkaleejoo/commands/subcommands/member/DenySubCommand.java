package org.zkaleejoo.commands.subcommands.member;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.commands.SubCommand;

public class DenySubCommand extends SubCommand {

    public DenySubCommand(OnlyClans plugin) {
        super(plugin, "deny");
    }

    @Override
    public String getPermission() {
        return "onlyclans.command.deny";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        plugin.getClanManager().denyInvite(player);
    }
}
