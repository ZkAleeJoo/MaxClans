package org.zkaleejoo.commands.subcommands.general;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.managers.ClanQuestManager;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.models.ClanQuest;
import org.zkaleejoo.models.ClanQuestProgress;
import org.zkaleejoo.utils.MessageUtils;
import org.zkaleejoo.utils.SoundUtils;

import java.util.List;

public class QuestsSubCommand extends SubCommand {

    public QuestsSubCommand(OnlyClans plugin) {
        super(plugin, "quests", "quest", "misiones", "mision");
    }

    @Override
    public String getPermission() {
        return "onlyclans.command.quests";
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

        ClanQuestManager qm = plugin.getClanQuestManager();
        if (qm == null || !qm.isEnabled()) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("quests-disabled", "&cThe clan quest system is currently disabled.")));
            return;
        }

        List<ClanQuest> dailyQuests = qm.getDailyQuests();

        String header = plugin.getMainConfigManager().getMessage("quests-header",
                "&#94A3B8&m━━━━━━━━━━━━━&r &#FDE047&lDAILY CLAN QUESTS &#94A3B8&m━━━━━━━━━━━━━");
        String subtitle = plugin.getMainConfigManager().getMessage("quests-subtitle",
                " &#94A3B8All clan members cooperate to complete these challenges!");
        String emptyMsg = plugin.getMainConfigManager().getMessage("quests-empty",
                " &7No daily quests are configured for today.");
        String completedStatus = plugin.getMainConfigManager().getMessage("quests-status-completed",
                "&#6EE7B7[COMPLETED ✔]");
        String inProgressStatus = plugin.getMainConfigManager().getMessage("quests-status-in-progress",
                "&#FDE047[IN PROGRESS ⌛]");
        String footer = plugin.getMainConfigManager().getMessage("quests-footer",
                "&#94A3B8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        player.sendMessage(MessageUtils.toComponent(header));
        player.sendMessage(MessageUtils.toComponent(subtitle));
        player.sendMessage(MessageUtils.toComponent(""));

        if (dailyQuests.isEmpty()) {
            player.sendMessage(MessageUtils.toComponent(emptyMsg));
        } else {
            for (ClanQuest quest : dailyQuests) {
                ClanQuestProgress progress = qm.getProgress(clan, quest);
                int current = progress != null ? progress.getProgress() : 0;
                int req = quest.getRequired();
                boolean completed = progress != null && progress.isCompleted();

                String status = completed ? completedStatus : inProgressStatus;
                String bar = buildProgressBar(current, req, 15);

                player.sendMessage(MessageUtils.toComponent(" " + quest.getName() + " " + status));
                player.sendMessage(MessageUtils.toComponent("  &7" + quest.getDescription()));
                player.sendMessage(MessageUtils.toComponent("  &#94A3B8[" + bar + "&#94A3B8] &#F8FAFC" + Math.min(current, req) + "&#94A3B8/&#F8FAFC" + req + " &#94A3B8| &#FDE047+" + quest.getRewardExp() + " Clan EXP"));
                player.sendMessage(MessageUtils.toComponent(""));
            }
        }

        player.sendMessage(MessageUtils.toComponent(footer));
        SoundUtils.playSound(player, "ITEM_BOOK_PAGE_TURN", 0.9f, 1.2f);
    }

    private String buildProgressBar(int current, int max, int bars) {
        if (max <= 0) max = 1;
        int filled = (int) Math.round(((double) Math.min(current, max) / max) * bars);
        filled = Math.max(0, Math.min(bars, filled));
        int empty = bars - filled;
        return "&#6EE7B7" + "█".repeat(filled) + "&#94A3B8" + "█".repeat(empty);
    }
}
