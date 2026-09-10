package org.zkaleejoo.commands.subcommands.general;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.MaxClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.utils.MessageUtils;

import java.util.Arrays;

public class ChatSubCommand extends SubCommand {

    public ChatSubCommand(MaxClans plugin) {
        super(plugin, "chat", "c");
    }

    @Override
    public String getPermission() {
        return "maxclans.command.chat";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("usage-chat-clan", "&cUsage: /clan chat <message>")));
            return;
        }

        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("not-in-clan", "&cYou are not in a clan.")));
            return;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        plugin.getClanManager().sendClanMessage(clan, player, message);
    }
}
