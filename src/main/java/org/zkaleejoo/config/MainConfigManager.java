package org.zkaleejoo.config;

import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;
import org.zkaleejoo.MaxClans;
import java.util.Arrays;

public class MainConfigManager {

    private CustomConfig configFile;
    private CustomConfig langFile;
    private CustomConfig menusFile;
    private CustomConfig placeholdersFile;
    private MaxClans plugin;

    private String selectedLanguage;
    private String prefix;
    private boolean updateCheckEnabled;
    private boolean bStatsEnabled;

    private String databaseType;
    private String databaseHost;
    private int databasePort;
    private String databaseName;
    private String databaseUsername;
    private String databasePassword;

    private String noPermission;
    private String pluginReload;
    private String subcommandInvalid;
    private String msgConsole;
    private String helpTitle;
    private List<String> helpLines;
    private String msgUpdateAvailable;
    private String msgUpdateCurrent;
    private String msgUpdateDownload;

    private int homeWarmupSeconds;
    private int homeCooldownSeconds;
    private boolean homeCancelOnMove;
    private boolean homeCancelOnDamage;
    private boolean homeAllowAlliesIfPublic;
    private int homeDefaultMax;
    private final java.util.NavigableMap<Integer, Integer> homeLevelMax = new java.util.TreeMap<>();

    public MainConfigManager(MaxClans plugin) {
        this.plugin = plugin;
        configFile = new CustomConfig("config.yml", null, plugin, false);
        configFile.registerConfig();

        menusFile = new CustomConfig("menus.yml", null, plugin, false);
        menusFile.registerConfig();

        placeholdersFile = new CustomConfig("placeholders.yml", null, plugin, false);
        placeholdersFile.registerConfig();

        new CustomConfig("quests.yml", null, plugin, false).registerConfig();

        new CustomConfig("messages_en.yml", "lang", plugin, false).registerConfig();
        new CustomConfig("messages_es.yml", "lang", plugin, false).registerConfig();

        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = configFile.getConfig();

        selectedLanguage = config.getString("general.language", "en");

        String langPath = "messages_" + selectedLanguage + ".yml";
        langFile = new CustomConfig(langPath, "lang", plugin, false);
        langFile.registerConfig();
        FileConfiguration lang = langFile.getConfig();

        prefix = config.getString("general.prefix", "&#7DD3FC&lMaxClans &#94A3B8» ");
        updateCheckEnabled = config.getBoolean("general.update-check", true);
        bStatsEnabled = config.getBoolean("general.bstats", true);

        databaseType = config.getString("database.type", "sqlite");
        databaseHost = config.getString("database.host", "localhost");
        databasePort = config.getInt("database.port", 3306);
        databaseName = config.getString("database.name", "maxclans");
        databaseUsername = config.getString("database.username", "root");
        databasePassword = config.getString("database.password", "");

        noPermission = lang.getString("messages.no-permission", "&cYou do not have permission.");
        pluginReload = lang.getString("messages.plugin-reload", "&aPlugin reloaded.");
        msgConsole = lang.getString("messages.message-console", "&cOnly players!");
        subcommandInvalid = lang.getString("messages.subcommand-invalid", "&cInvalid subcommand.");
        helpTitle = lang.getString("messages.command-help-title", "&6MaxClans Help");
        helpLines = lang.getStringList("messages.command-help-list");
        if (helpLines == null || helpLines.isEmpty()) {
            helpLines = Arrays.asList("&a/clan help", "&a/clan create <name>", "&a/clan invite <player>");
        }
        msgUpdateAvailable = lang.getString("messages.update-available", "&eNew version: {version}");
        msgUpdateCurrent = lang.getString("messages.update-current", "&7Current: {version}");
        msgUpdateDownload = lang.getString("messages.update-download", "&eDownload it!");

        homeWarmupSeconds = config.getInt("homes.warmup-seconds", 3);
        homeCooldownSeconds = config.getInt("homes.cooldown-seconds", 300);
        homeCancelOnMove = config.getBoolean("homes.cancel-on-move", true);
        homeCancelOnDamage = config.getBoolean("homes.cancel-on-damage", true);
        homeAllowAlliesIfPublic = config.getBoolean("homes.allow-allies-if-public", true);
        homeDefaultMax = config.getInt("homes.default-max-homes", 1);

        homeLevelMax.clear();
        org.bukkit.configuration.ConfigurationSection levelsSec = config.getConfigurationSection("homes.levels");
        if (levelsSec != null) {
            for (String key : levelsSec.getKeys(false)) {
                try {
                    int lvl = Integer.parseInt(key);
                    int maxHomes = levelsSec.getInt(key);
                    homeLevelMax.put(lvl, maxHomes);
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    public void reloadConfig() {
        configFile.reloadConfig();
        menusFile.reloadConfig();
        if (placeholdersFile != null) {
            placeholdersFile.reloadConfig();
        }
        loadConfig();
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean isUpdateCheckEnabled() {
        return updateCheckEnabled;
    }

    public boolean isBStatsEnabled() {
        return bStatsEnabled;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public String getDatabaseHost() {
        return databaseHost;
    }

    public int getDatabasePort() {
        return databasePort;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getDatabaseUsername() {
        return databaseUsername;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public String getNoPermission() {
        return noPermission;
    }

    public String getPluginReload() {
        return pluginReload;
    }

    public String getMsgConsole() {
        return msgConsole;
    }

    public String getSubcommandInvalid() {
        return subcommandInvalid;
    }

    public String getHelpTitle() {
        return helpTitle;
    }

    public List<String> getHelpLines() {
        return helpLines;
    }

    public String getMsgUpdateAvailable() {
        return msgUpdateAvailable;
    }

    public String getMsgUpdateCurrent() {
        return msgUpdateCurrent;
    }

    public String getMsgUpdateDownload() {
        return msgUpdateDownload;
    }

    public String getSelectedLanguage() {
        return selectedLanguage;
    }

    public FileConfiguration getConfigFile() {
        return configFile.getConfig();
    }

    public FileConfiguration getMenusConfig() {
        return menusFile.getConfig();
    }

    public CustomConfig getPlaceholdersFile() {
        return placeholdersFile;
    }

    public FileConfiguration getPlaceholdersConfig() {
        return placeholdersFile != null ? placeholdersFile.getConfig() : null;
    }

    public String getMessage(String path, String def) {
        String msg = langFile.getConfig().getString("messages." + path, def);
        return msg;
    }

    public String getMessage(String path) {
        return getMessage(path, "&cMessage not found: " + path);
    }

    public int getHomeWarmupSeconds() {
        return homeWarmupSeconds;
    }

    public int getHomeCooldownSeconds() {
        return homeCooldownSeconds;
    }

    public boolean isHomeCancelOnMove() {
        return homeCancelOnMove;
    }

    public boolean isHomeCancelOnDamage() {
        return homeCancelOnDamage;
    }

    public boolean isHomeAllowAlliesIfPublic() {
        return homeAllowAlliesIfPublic;
    }

    public int getHomeDefaultMax() {
        return homeDefaultMax;
    }

    public int getMaxHomesForLevel(int level) {
        if (homeLevelMax.isEmpty()) {
            return Math.max(1, homeDefaultMax);
        }
        Integer exact = homeLevelMax.get(level);
        if (exact != null) {
            return Math.max(1, exact);
        }
        java.util.Map.Entry<Integer, Integer> floor = homeLevelMax.floorEntry(level);
        if (floor != null) {
            return Math.max(1, floor.getValue());
        }
        return Math.max(1, homeDefaultMax);
    }
}
