package org.zkaleejoo.commands.subcommands.member;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.MaxClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class InviteSubCommand extends SubCommand {

    public InviteSubCommand(MaxClans plugin) {
        super(plugin, "invite");
    }

    @Override
    public String getPermission() {
        return "maxclans.command.invite";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("usage-invite", "&cUsage: /clan invite <player>")));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("player-not-found", "&cPlayer not found or offline.")));
            return;
        }

        plugin.getClanManager().invitePlayer(player, target);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            List<String> completions = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                completions.add(p.getName());
            }
            return filterCompletions(completions, args[1]);
        }
        return super.tabComplete(sender, args);
    }
}
