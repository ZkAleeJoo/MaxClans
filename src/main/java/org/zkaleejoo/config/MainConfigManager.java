package org.zkaleejoo.config;

import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;
import org.zkaleejoo.OnlyClans;
import java.util.Arrays;

public class MainConfigManager {

    private CustomConfig configFile;
    private CustomConfig langFile;
    private OnlyClans plugin;
    
    private String selectedLanguage;
    private String prefix;
    private boolean updateCheckEnabled;
    private boolean bStatsEnabled;
    
    // Messages
    private String noPermission;
    private String pluginReload;
    private String subcommandInvalid;
    private String msgConsole;
    private String helpTitle;
    private List<String> helpLines;
    private String msgUpdateAvailable;
    private String msgUpdateCurrent;
    private String msgUpdateDownload;

    public MainConfigManager(OnlyClans plugin) {
        this.plugin = plugin;
        configFile = new CustomConfig("config.yml", null, plugin, false);
        configFile.registerConfig();
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = configFile.getConfig();

        selectedLanguage = config.getString("general.language", "en");

        String langPath = "messages_" + selectedLanguage + ".yml";
        langFile = new CustomConfig(langPath, "lang", plugin, false);
        langFile.registerConfig();
        FileConfiguration lang = langFile.getConfig();

        prefix = config.getString("general.prefix", "&#8727F5&lOnlyClans &8» ");
        updateCheckEnabled = config.getBoolean("general.update-check", true);
        bStatsEnabled = config.getBoolean("general.bstats", true);
        
        noPermission = lang.getString("messages.no-permission", "&cYou do not have permission.");
        pluginReload = lang.getString("messages.plugin-reload", "&aPlugin reloaded.");
        msgConsole = lang.getString("messages.message-console", "&cOnly players!");
        subcommandInvalid = lang.getString("messages.subcommand-invalid", "&cInvalid subcommand.");
        helpTitle = lang.getString("messages.command-help-title", "&6OnlyClans Help");
        helpLines = lang.getStringList("messages.command-help-list");
        if (helpLines == null || helpLines.isEmpty()) {
            helpLines = Arrays.asList("&a/onlyclans reload", "&a/onlyclans help");
        }
        msgUpdateAvailable = lang.getString("messages.update-available", "&eNew version: {version}");
        msgUpdateCurrent = lang.getString("messages.update-current", "&7Current: {version}");
        msgUpdateDownload = lang.getString("messages.update-download", "&eDownload it!");
    }

    public void reloadConfig() {
        configFile.reloadConfig();
        loadConfig();
    }
    
    public String getPrefix() { return prefix; }
    public boolean isUpdateCheckEnabled() { return updateCheckEnabled; }
    public boolean isBStatsEnabled() { return bStatsEnabled; }
    
    public String getNoPermission() { return noPermission; }
    public String getPluginReload() { return pluginReload; }
    public String getMsgConsole() { return msgConsole; }
    public String getSubcommandInvalid() { return subcommandInvalid; }
    public String getHelpTitle() { return helpTitle; }
    public List<String> getHelpLines() { return helpLines; }
    public String getMsgUpdateAvailable() { return msgUpdateAvailable; }
    public String getMsgUpdateCurrent() { return msgUpdateCurrent; }
    public String getMsgUpdateDownload() { return msgUpdateDownload; }
}
