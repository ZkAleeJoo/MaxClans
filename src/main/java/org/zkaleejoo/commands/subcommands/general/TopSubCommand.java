package org.zkaleejoo.commands.subcommands.general;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.models.TopSortType;
import org.zkaleejoo.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class TopSubCommand extends SubCommand {

    public TopSubCommand(OnlyClans plugin) {
        super(plugin, "top", "leaderboard");
    }

    @Override
    public String getPermission() {
        return "onlyclans.command.top";
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        TopSortType sortType = TopSortType.KDR;
        if (args.length >= 2) {
            TopSortType parsed = TopSortType.fromKey(args[1]);
            if (parsed != null) {
                sortType = parsed;
            }
        }

        if (!(sender instanceof Player player)) {
            sendConsoleTop(sender, sortType);
            return;
        }

        plugin.getMenuBuilder().openTopMenu(player, sortType, 0);
    }

    private void sendConsoleTop(CommandSender sender, TopSortType sortType) {
        List<Clan> top = plugin.getClanManager().getTopClans(sortType);
        String typeLabel = sortType.name();
        sender.sendMessage(MessageUtils.toComponent(
                plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                        .getMessage("top-header", "&6&lTop Clans &7({sort})")
                        .replace("{sort}", typeLabel)));
        if (top.isEmpty()) {
            sender.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getMessage("top-empty", "&7No clans registered yet.")));
            return;
        }
        int limit = Math.min(10, top.size());
        for (int i = 0; i < limit; i++) {
            Clan c = top.get(i);
            sender.sendMessage(MessageUtils.toComponent(
                    "&e#" + (i + 1) + " &f" + c.getName() + " &7[" + c.getTag() + "] &8- &7KDR: &a"
                            + c.getFormattedKDR()
                            + " &7| Kills: &c" + c.getKills() + " &7| Deaths: &4" + c.getDeaths()
                            + " &7| Members: &b" + c.getMemberCount()));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            List<String> completions = new ArrayList<>();
            completions.add("kdr");
            completions.add("kills");
            completions.add("members");
            return filterCompletions(completions, args[1]);
        }
        return super.tabComplete(sender, args);
    }
}
