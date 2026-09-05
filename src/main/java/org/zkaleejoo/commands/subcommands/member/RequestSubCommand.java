package org.zkaleejoo.commands.subcommands.member;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class RequestSubCommand extends SubCommand {

    public RequestSubCommand(OnlyClans plugin) {
        super(plugin, "request", "join");
    }

    @Override
    public String getPermission() {
        return "onlyclans.command.request";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("usage-request", "&cUsage: /clan request <clan>")));
            return;
        }

        String clanName = args[1];
        Clan targetClan = plugin.getClanManager().getClanByName(clanName);
        if (targetClan == null) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("clan-not-exists", "&cThat clan no longer exists.")));
            return;
        }

        plugin.getClanManager().requestToJoinClan(player, targetClan);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            List<String> completions = new ArrayList<>();
            for (Clan c : plugin.getClanManager().getAllClans()) {
                completions.add(c.getName());
            }
            return filterCompletions(completions, args[1]);
        }
        return super.tabComplete(sender, args);
    }
}
