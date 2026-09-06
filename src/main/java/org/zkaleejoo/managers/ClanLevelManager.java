package org.zkaleejoo.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.models.ClanLevelConfig;
import org.zkaleejoo.utils.MessageUtils;
import org.zkaleejoo.utils.SoundUtils;

import java.util.*;

public class ClanLevelManager {

    private final OnlyClans plugin;
    private final NavigableMap<Integer, ClanLevelConfig> levelConfigs = new TreeMap<>();
    private final Map<String, Integer> monsterExp = new HashMap<>();
    private final Map<String, Integer> miningExp = new HashMap<>();
    private int monsterDefaultExp = 10;
    private int monsterBossExp = 100;
    private int woodCuttingDefaultExp = 2;
    private int miningDefaultExp = 1;
    private int pvpKillPlayerExp = 25;
    private int pvpKillRivalExp = 60;
    private boolean actionbarOnExp = true;
    private String expSound = "ENTITY_EXPERIENCE_ORB_PICKUP";
    private String levelUpSound = "UI_TOAST_CHALLENGE_COMPLETE";

    public ClanLevelManager(OnlyClans plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        levelConfigs.clear();
        monsterExp.clear();
        miningExp.clear();

        ConfigurationSection section = plugin.getMainConfigManager().getConfigFile()
                .getConfigurationSection("leveling");
        if (section == null) {
            initDefaultLevels();
            return;
        }

        actionbarOnExp = section.getBoolean("actionbar-on-exp", true);
        expSound = section.getString("exp-sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
        levelUpSound = section.getString("levelup-sound", "UI_TOAST_CHALLENGE_COMPLETE");

        ConfigurationStyleExpSources(section.getConfigurationSection("exp-sources"));

        ConfigurationSection levelsSec = section.getConfigurationSection("levels");
        if (levelsSec != null) {
            for (String key : levelsSec.getKeys(false)) {
                try {
                    int lvl = Integer.parseInt(key);
                    ConfigurationSection lvlSec = levelsSec.getConfigurationSection(key);
                    if (lvlSec != null) {
                        int expReq = lvlSec.getInt("exp-required", 0);
                        int maxMembers = lvlSec.getInt("max-members", 8);
                        int maxAllies = lvlSec.getInt("max-allies", 0);
                        int maxHomes = lvlSec.getInt("max-homes", 1);
                        boolean bank = lvlSec.getBoolean("bank-access", false);
                        boolean chest = lvlSec.getBoolean("chest-access", false);
                        int chestRows = lvlSec.getInt("chest-rows", 3);
                        boolean baseEffect = lvlSec.getBoolean("base-effect", false);
                        String effectType = lvlSec.getString("base-effect-type", "HASTE");
                        int effectAmp = lvlSec.getInt("base-effect-amplifier", 0);
                        int effectRadius = lvlSec.getInt("base-effect-radius", 30);

                        ClanLevelConfig cfg = new ClanLevelConfig(lvl, expReq, maxMembers, maxAllies, maxHomes,
                                bank, chest, chestRows, baseEffect, effectType, effectAmp, effectRadius);
                        levelConfigs.put(lvl, cfg);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (levelConfigs.isEmpty()) {
            initDefaultLevels();
        }
    }

    private void ConfigurationStyleExpSources(ConfigurationSection expSources) {
        if (expSources == null)
            return;

        ConfigurationSection monsters = expSources.getConfigurationSection("monsters");
        if (monsters != null) {
            monsterDefaultExp = monsters.getInt("default", 10);
            monsterBossExp = monsters.getInt("boss", 100);
            ConfigurationSection custom = monsters.getConfigurationSection("custom");
            if (custom != null) {
                for (String mob : custom.getKeys(false)) {
                    monsterExp.put(mob.toUpperCase(), custom.getInt(mob));
                }
            }
        }

        ConfigurationSection wood = expSources.getConfigurationSection("wood-cutting");
        if (wood != null) {
            woodCuttingDefaultExp = wood.getInt("default", 2);
        }

        ConfigurationSection mining = expSources.getConfigurationSection("mining");
        if (mining != null) {
            miningDefaultExp = mining.getInt("default", 1);
            ConfigurationSection custom = mining.getConfigurationSection("custom");
            if (custom != null) {
                for (String ore : custom.getKeys(false)) {
                    miningExp.put(ore.toUpperCase(), custom.getInt(ore));
                }
            }
        }

        ConfigurationSection pvp = expSources.getConfigurationSection("pvp");
        if (pvp != null) {
            pvpKillPlayerExp = pvp.getInt("kill-player", 25);
            pvpKillRivalExp = pvp.getInt("kill-rival", 60);
        }
    }

    private void initDefaultLevels() {
        levelConfigs.put(1, new ClanLevelConfig(1, 0, 8, 0, 1, false, false, 3, false, "HASTE", 0, 30));
        levelConfigs.put(2, new ClanLevelConfig(2, 1500, 12, 0, 2, true, false, 3, false, "HASTE", 0, 30));
        levelConfigs.put(3, new ClanLevelConfig(3, 4000, 12, 0, 3, true, true, 3, false, "HASTE", 0, 30));
        levelConfigs.put(4, new ClanLevelConfig(4, 8000, 16, 1, 4, true, true, 3, false, "HASTE", 0, 30));
        levelConfigs.put(5, new ClanLevelConfig(5, 15000, 20, 1, 5, true, true, 4, true, "HASTE", 0, 30));
        levelConfigs.put(6, new ClanLevelConfig(6, 25000, 24, 2, 6, true, true, 4, true, "HASTE", 0, 35));
        levelConfigs.put(7, new ClanLevelConfig(7, 40000, 28, 2, 7, true, true, 5, true, "HASTE", 0, 40));
        levelConfigs.put(8, new ClanLevelConfig(8, 60000, 32, 3, 8, true, true, 5, true, "REGENERATION", 0, 45));
        levelConfigs.put(9, new ClanLevelConfig(9, 85000, 36, 4, 9, true, true, 6, true, "REGENERATION", 0, 50));
        levelConfigs.put(10, new ClanLevelConfig(10, 120000, 50, 5, 10, true, true, 6, true, "REGENERATION", 0, 60));
    }

    public int getMaxLevel() {
        return levelConfigs.isEmpty() ? 10 : levelConfigs.lastKey();
    }

    public ClanLevelConfig getLevelConfig(int level) {
        if (levelConfigs.isEmpty()) {
            initDefaultLevels();
        }
        ClanLevelConfig exact = levelConfigs.get(level);
        if (exact != null) {
            return exact;
        }
        Map.Entry<Integer, ClanLevelConfig> floor = levelConfigs.floorEntry(level);
        if (floor != null) {
            return floor.getValue();
        }
        return levelConfigs.firstEntry().getValue();
    }

    public int getExpRequiredForLevel(int level) {
        ClanLevelConfig config = levelConfigs.get(level);
        return config != null ? config.getExpRequired() : 0;
    }

    public int getExpForNextLevel(Clan clan) {
        if (clan == null)
            return 0;
        int nextLevel = clan.getLevel() + 1;
        if (nextLevel > getMaxLevel()) {
            return 0;
        }
        ClanLevelConfig nextConfig = levelConfigs.get(nextLevel);
        return nextConfig != null ? nextConfig.getExpRequired() : 0;
    }

    public double getExpPercentage(Clan clan) {
        if (clan == null)
            return 0.0;
        if (clan.getLevel() >= getMaxLevel()) {
            return 100.0;
        }
        int required = getExpForNextLevel(clan);
        if (required <= 0)
            return 100.0;
        return Math.min(100.0, ((double) clan.getExp() / required) * 100.0);
    }

    public String getExpProgressBar(Clan clan, int totalBars) {
        if (clan == null)
            return "";
        int current = clan.getExp();
        int required = getExpForNextLevel(clan);
        if (clan.getLevel() >= getMaxLevel()) {
            return "&#6EE7B7" + "█".repeat(Math.max(1, totalBars));
        }
        if (required <= 0)
            required = 1;
        int filled = (int) Math.round(((double) current / required) * totalBars);
        filled = Math.max(0, Math.min(totalBars, filled));
        int unfilled = totalBars - filled;

        return "&#6EE7B7" + "█".repeat(filled) + "&#94A3B8" + "█".repeat(unfilled);
    }

    public int getMaxMembers(int level) {
        return getLevelConfig(level).getMaxMembers();
    }

    public int getMaxAllies(int level) {
        return getLevelConfig(level).getMaxAllies();
    }

    public int getMaxHomes(int level) {
        return getLevelConfig(level).getMaxHomes();
    }

    public boolean hasBankAccess(int level) {
        return getLevelConfig(level).hasBankAccess();
    }

    public boolean hasChestAccess(int level) {
        return getLevelConfig(level).hasChestAccess();
    }

    public int getChestRows(int level) {
        return getLevelConfig(level).getChestRows();
    }

    public boolean hasBaseEffect(int level) {
        return getLevelConfig(level).hasBaseEffect();
    }

    public int getMonsterExp(String entityType, boolean isBoss) {
        if (entityType != null && monsterExp.containsKey(entityType.toUpperCase())) {
            return monsterExp.get(entityType.toUpperCase());
        }
        return isBoss ? monsterBossExp : monsterDefaultExp;
    }

    public int getWoodCuttingExp(String materialName) {
        return woodCuttingDefaultExp;
    }

    public int getMiningExp(String materialName) {
        if (materialName != null && miningExp.containsKey(materialName.toUpperCase())) {
            return miningExp.get(materialName.toUpperCase());
        }
        return miningDefaultExp;
    }

    public int getPvPExp(boolean isRival) {
        return isRival ? pvpKillRivalExp : pvpKillPlayerExp;
    }

    public void addExp(Clan clan, int amount, String source, Player contributor) {
        if (clan == null || amount <= 0)
            return;

        if (clan.getLevel() >= getMaxLevel()) {
            return;
        }

        clan.addExp(amount);

        if (contributor != null && contributor.isOnline()) {
            if (actionbarOnExp) {
                String sourceFormatted = source != null ? source
                        : plugin.getMainConfigManager().getMessage("exp-source-activity", "Activity");
                String barMsg = plugin.getMainConfigManager().getMessage("exp-actionbar",
                        "&#6EE7B7+{exp} Clan EXP &#94A3B8(&f{source}&#94A3B8)")
                        .replace("{exp}", String.valueOf(amount))
                        .replace("{source}", sourceFormatted);
                contributor.sendActionBar(MessageUtils.toComponent(barMsg));
            }
            if (expSound != null && !expSound.equalsIgnoreCase("none")) {
                SoundUtils.playSound(contributor, expSound, 0.6f, 1.4f);
            }
        }

        checkLevelUp(clan);
        plugin.getClanStorage().updateClan(clan);
    }

    public void checkLevelUp(Clan clan) {
        if (clan == null)
            return;

        int maxLvl = getMaxLevel();
        boolean leveledUp = false;

        while (clan.getLevel() < maxLvl) {
            int nextLvl = clan.getLevel() + 1;
            int required = getExpRequiredForLevel(nextLvl);
            if (required <= 0 || clan.getExp() < required) {
                break;
            }

            clan.setExp(clan.getExp() - required);
            clan.setLevel(nextLvl);
            leveledUp = true;

            broadcastLevelUp(clan, nextLvl);
        }

        if (leveledUp) {
            plugin.getClanStorage().updateClan(clan);
        }
    }

    private void broadcastLevelUp(Clan clan, int newLevel) {
        ClanLevelConfig config = getLevelConfig(newLevel);

        List<String> perks = new ArrayList<>();
        perks.add(plugin.getMainConfigManager().getMessage("perk-members", "&aMember limit: &f{max}")
                .replace("{max}", String.valueOf(config.getMaxMembers())));
        perks.add(plugin.getMainConfigManager().getMessage("perk-allies", "&aAllies allowed: &f{max}")
                .replace("{max}", String.valueOf(config.getMaxAllies())));
        perks.add(plugin.getMainConfigManager().getMessage("perk-homes", "&aClan homes: &f{max}").replace("{max}",
                String.valueOf(config.getMaxHomes())));
        if (config.hasBankAccess()) {
            perks.add(plugin.getMainConfigManager().getMessage("perk-bank", "&aAccess to Clan Bank (/clan bank)"));
        }
        if (config.hasChestAccess()) {
            perks.add(plugin.getMainConfigManager()
                    .getMessage("perk-chest", "&aShared Clan Chest unlocked ({rows} rows)")
                    .replace("{rows}", String.valueOf(config.getChestRows())));
        }
        if (config.hasBaseEffect()) {
            perks.add(plugin.getMainConfigManager()
                    .getMessage("perk-base-effect", "&aPermanent base effect: &f{effect} (Radius {radius}m)")
                    .replace("{effect}", config.getBaseEffectType())
                    .replace("{radius}", String.valueOf(config.getBaseEffectRadius())));
        }

        String prefix = plugin.getMainConfigManager().getPrefix();
        String header = plugin.getMainConfigManager().getMessage("level-up-header",
                "&#94A3B8&m━━━━━━━━━━━━━&r &#FDE047&lCLAN LEVEL UP! &#94A3B8&m━━━━━━━━━━━━━");
        String announcement = plugin.getMainConfigManager().getMessage("level-up-broadcast",
                "&#6EE7B7Clan &f{clan}&#6EE7B7 has reached &#FDE047&lLevel {level}&#6EE7B7!")
                .replace("{clan}", clan.getName())
                .replace("{level}", String.valueOf(newLevel));
        String perksTitle = plugin.getMainConfigManager().getMessage("level-up-perks-title",
                "&#7DD3FCUnlocked Perks & Abilities:");
        String footer = plugin.getMainConfigManager().getMessage("level-up-footer",
                "&#94A3B8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        for (UUID uuid : clan.getMembers().keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.sendMessage(MessageUtils.toComponent(header));
                p.sendMessage(MessageUtils.toComponent(prefix + announcement));
                p.sendMessage(MessageUtils.toComponent(perksTitle));
                for (String perk : perks) {
                    p.sendMessage(MessageUtils.toComponent(" &#7DD3FC✦ " + perk));
                }
                p.sendMessage(MessageUtils.toComponent(footer));

                if (levelUpSound != null && !levelUpSound.equalsIgnoreCase("none")) {
                    SoundUtils.playSound(p, levelUpSound, 1.0f, 1.0f);
                }
            }
        }
    }
}
