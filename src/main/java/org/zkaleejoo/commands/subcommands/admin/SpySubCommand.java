package org.zkaleejoo.commands.subcommands.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.MaxClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.managers.ClanManager;
import org.zkaleejoo.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class SpySubCommand extends SubCommand {

    public SpySubCommand(MaxClans plugin) {
        super(plugin, "spy");
    }

    @Override
    public String getPermission() {
        return "maxclans.spy";
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("maxclans.spy") || sender.hasPermission("maxclans.admin");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        ClanManager clanManager = plugin.getClanManager();

        boolean newState;
        if (args.length >= 2) {
            String state = args[1].toLowerCase();
            newState = state.equals("on") || state.equals("true") || state.equals("enable")
                    || state.equals("1");
            clanManager.setSpyMode(player.getUniqueId(), newState);
        } else {
            newState = clanManager.toggleSpyMode(player.getUniqueId());
        }

        if (newState) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("spy-enabled", "&aClan chat spy mode enabled.")));
        } else {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("spy-disabled", "&cClan chat spy mode disabled.")));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            List<String> completions = new ArrayList<>();
            completions.add("on");
            completions.add("off");
            return filterCompletions(completions, args[1]);
        }
        return super.tabComplete(sender, args);
    }
}
