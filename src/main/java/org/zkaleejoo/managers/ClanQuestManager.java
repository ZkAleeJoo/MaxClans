package org.zkaleejoo.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.zkaleejoo.MaxClans;
import org.zkaleejoo.config.CustomConfig;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.models.ClanQuest;
import org.zkaleejoo.models.ClanQuestProgress;
import org.zkaleejoo.utils.MessageUtils;
import org.zkaleejoo.utils.SoundUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClanQuestManager {

    private final MaxClans plugin;
    private CustomConfig questsFile;
    private final Map<String, ClanQuest> registeredQuests = new LinkedHashMap<>();
    private final Map<String, Map<String, ClanQuestProgress>> progressCache = new ConcurrentHashMap<>();
    private volatile String cachedDate;
    private boolean enabled = true;
    private int dailyQuestsAmount = 3;

    public ClanQuestManager(MaxClans plugin) {
        this.plugin = plugin;
        this.cachedDate = getCurrentDateString();
        this.questsFile = new CustomConfig("quests.yml", null, plugin, false);
        this.questsFile.registerConfig();
        loadConfig();
    }

    public void loadConfig() {
        if (questsFile != null) {
            questsFile.reloadConfig();
        }
        registeredQuests.clear();
        progressCache.clear();
        cachedDate = getCurrentDateString();

        ConfigurationSection config = questsFile.getConfig();
        if (config == null)
            return;

        enabled = config.getBoolean("settings.enabled", true);
        dailyQuestsAmount = config.getInt("settings.daily-quests-amount", 3);

        ConfigurationSection questsSec = config.getConfigurationSection("quests");
        if (questsSec != null) {
            for (String key : questsSec.getKeys(false)) {
                ConfigurationSection q = questsSec.getConfigurationSection(key);
                if (q != null) {
                    String name = q.getString("name", key);
                    String desc = q.getString("description", "");
                    ClanQuest.QuestType type = ClanQuest.QuestType.fromString(q.getString("type", "KILL_MOB"));
                    String target = q.getString("target", "ANY");
                    int required = q.getInt("required", 100);
                    int rewardExp = q.getInt("reward-exp", 500);
                    String icon = q.getString("icon", "BOOK");

                    ClanQuest quest = new ClanQuest(key, name, desc, type, target, required, rewardExp, icon);
                    registeredQuests.put(key, quest);
                }
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getCurrentDateString() {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private void checkDateRollover(String currentDate) {
        if (!currentDate.equals(cachedDate)) {
            synchronized (progressCache) {
                if (!currentDate.equals(cachedDate)) {
                    cachedDate = currentDate;
                    progressCache.clear();
                }
            }
        }
    }

    public List<ClanQuest> getDailyQuests() {
        if (registeredQuests.isEmpty()) {
            return Collections.emptyList();
        }

        List<ClanQuest> all = new ArrayList<>(registeredQuests.values());
        if (all.size() <= dailyQuestsAmount) {
            return all;
        }

        int dayOfYear = LocalDate.now().getDayOfYear();
        int year = LocalDate.now().getYear();
        Random rng = new Random((long) year * 1000L + dayOfYear);

        List<ClanQuest> shuffled = new ArrayList<>(all);
        Collections.shuffle(shuffled, rng);
        return shuffled.subList(0, Math.min(dailyQuestsAmount, shuffled.size()));
    }

    public ClanQuestProgress getProgress(Clan clan, ClanQuest quest) {
        if (clan == null || quest == null)
            return null;
        String date = getCurrentDateString();
        checkDateRollover(date);
        String clanKey = clan.getName().toLowerCase();
        String cacheKey = date + ":" + clanKey;

        Map<String, ClanQuestProgress> clanMap = progressCache.computeIfAbsent(cacheKey,
                k -> plugin.getClanStorage().loadQuestProgressForClan(clan.getName(), date));

        return clanMap.computeIfAbsent(quest.getId(),
                id -> new ClanQuestProgress(clan.getName(), quest.getId(), 0, false, date));
    }

    public void incrementProgress(Clan clan, ClanQuest.QuestType type, String target, int amount) {
        if (!enabled || clan == null || amount <= 0)
            return;

        List<ClanQuest> dailyQuests = getDailyQuests();
        for (ClanQuest quest : dailyQuests) {
            if (quest.getType() != type)
                continue;

            if (matchesTarget(quest, target)) {
                ClanQuestProgress prog = getProgress(clan, quest);
                if (prog != null && !prog.isCompleted()) {
                    prog.addProgress(amount);

                    if (prog.getProgress() >= quest.getRequired()) {
                        prog.setCompleted(true);
                        plugin.getClanStorage().saveQuestProgress(prog);

                        completeQuest(clan, quest);
                    } else {
                        plugin.getClanStorage().saveQuestProgress(prog);
                    }
                }
            }
        }
    }

    private boolean matchesTarget(ClanQuest quest, String target) {
        String qTarget = quest.getTarget().toUpperCase();
        if (qTarget.equals("ANY") || qTarget.equals("ALL"))
            return true;
        if (target == null)
            return false;

        String t = target.toUpperCase();
        if (qTarget.equals(t))
            return true;

        if (quest.getType() == ClanQuest.QuestType.KILL_MOB) {
            if (qTarget.equals("MONSTER")) {
                return !t.equals("PLAYER") && !t.equals("ANIMAL");
            }
        } else if (quest.getType() == ClanQuest.QuestType.CHOP_WOOD) {
            if (qTarget.equals("LOGS") || qTarget.equals("WOOD")) {
                return t.endsWith("_LOG") || t.endsWith("_WOOD") || t.endsWith("_STEM") || t.endsWith("_HYPHAE");
            }
        } else if (quest.getType() == ClanQuest.QuestType.MINE_BLOCK) {
            if (qTarget.equals("ORES") || qTarget.equals("ORE")) {
                return t.endsWith("_ORE") || t.equals("ANCIENT_DEBRIS");
            }
        }

        return false;
    }

    private void completeQuest(Clan clan, ClanQuest quest) {
        String prefix = plugin.getMainConfigManager().getPrefix();
        String header = plugin.getMainConfigManager().getMessage("quest-completed-header",
                "&#94A3B8&m━━━━━━━━━━━━━&r &#6EE7B7&lCLAN QUEST COMPLETED! &#94A3B8&m━━━━━━━━━━━━━");
        String msg = plugin.getMainConfigManager().getMessage("quest-completed-broadcast",
                "&#6EE7B7Your clan has completed the daily quest &f{quest}&#6EE7B7! Reward: &#FDE047+{exp} Clan EXP")
                .replace("{quest}", quest.getName())
                .replace("{exp}", String.valueOf(quest.getRewardExp()));
        String footer = plugin.getMainConfigManager().getMessage("quest-completed-footer",
                "&#94A3B8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        for (UUID uuid : clan.getMembers().keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.sendMessage(MessageUtils.toComponent(header));
                p.sendMessage(MessageUtils.toComponent(prefix + msg));
                p.sendMessage(MessageUtils.toComponent(footer));
                SoundUtils.playSound(p, "UI_TOAST_CHALLENGE_COMPLETE", 1.0f, 1.2f);
            }
        }

        plugin.getClanLevelManager().addExp(clan, quest.getRewardExp(), "Quest: " + quest.getName(), null);
    }
}
