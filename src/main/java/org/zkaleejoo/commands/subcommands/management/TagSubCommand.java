package org.zkaleejoo.commands.subcommands.management;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.MaxClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.utils.MessageUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TagSubCommand extends SubCommand {

    public TagSubCommand(MaxClans plugin) {
        super(plugin, "tag", "settag");
    }

    @Override
    public String getPermission() {
        return "maxclans.command.tag";
    }

    @Override
    public List<String> getTabSuggestions(CommandSender sender) {
        if (!hasPermission(sender)) {
            return Collections.emptyList();
        }
        return List.of("tag", "settag");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("not-in-clan", "&cYou are not in a clan.")));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("usage-tag", "&cUsage: /clan tag <tag>")));
            return;
        }

        String newTag = args[1];
        plugin.getClanManager().setClanTag(player, clan, newTag);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            List<String> completions = new ArrayList<>();
            if (sender instanceof Player p) {
                Clan clan = plugin.getClanManager().getClanByPlayer(p.getUniqueId());
                if (clan != null) {
                    completions.add(clan.getTag());
                }
            }
            return filterCompletions(completions, args[1]);
        }
        return super.tabComplete(sender, args);
    }
}
