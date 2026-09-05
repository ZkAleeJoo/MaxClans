package org.zkaleejoo.commands.subcommands.management;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.utils.MessageUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DecorSubCommand extends SubCommand {

    public DecorSubCommand(OnlyClans plugin) {
        super(plugin, "decor", "displayname", "color", "namecolor");
    }

    @Override
    public String getPermission() {
        return "onlyclans.command.decor";
    }

    @Override
    public List<String> getTabSuggestions(CommandSender sender) {
        if (!hasPermission(sender)) {
            return Collections.emptyList();
        }
        return List.of("decor", "displayname", "color");
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
                            .getMessage("usage-decor", "&cUsage: /clan decor <name_with_colors|reset>")));
            return;
        }
        String decoratedName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        plugin.getClanManager().setClanDisplayName(player, clan, decoratedName);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            List<String> completions = new ArrayList<>();
            completions.add("reset");
            if (sender instanceof Player p) {
                Clan clan = plugin.getClanManager().getClanByPlayer(p.getUniqueId());
                if (clan != null) {
                    completions.add(clan.getName());
                }
            }
            return filterCompletions(completions, args[1]);
        }
        return super.tabComplete(sender, args);
    }
}
