package org.zkaleejoo;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;
import org.zkaleejoo.commands.ChatCommand;
import org.zkaleejoo.commands.MainCommand;
import org.zkaleejoo.config.MainConfigManager;
import org.zkaleejoo.database.ClanStorage;
import org.zkaleejoo.database.DatabaseManager;
import org.zkaleejoo.gui.MenuBuilder;
import org.zkaleejoo.listeners.ClanChatListener;
import org.zkaleejoo.listeners.ClanDamageListener;
import org.zkaleejoo.listeners.MenuListener;
import org.zkaleejoo.listeners.PlayerJoinListener;
import org.zkaleejoo.managers.ClanManager;
import org.zkaleejoo.managers.PlaceholderManager;
import org.zkaleejoo.utils.FoliaCompat;
import org.zkaleejoo.utils.FoliaCompat.WrappedTask;
import org.zkaleejoo.utils.MessageUtils;
import org.zkaleejoo.utils.UpdateChecker;

public class OnlyClans extends JavaPlugin {

    private static final int BSTATS_PLUGIN_ID = 33651;
    private static final long UPDATE_CHECK_INTERVAL_TICKS = 20L * 60L * 60L * 5L;

    private MainConfigManager mainConfigManager;
    private PlaceholderManager placeholderManager;
    private DatabaseManager databaseManager;
    private ClanStorage clanStorage;
    private ClanManager clanManager;
    private MenuBuilder menuBuilder;
    private String latestVersion;
    private Metrics metrics;
    private WrappedTask updateCheckTask;

    @Override
    public void onEnable() {
        mainConfigManager = new MainConfigManager(this);
        placeholderManager = new PlaceholderManager(this);
        placeholderManager.loadConfig(mainConfigManager.getConfigFile());
        syncMetricsState();

        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();

        clanStorage = new ClanStorage(this, databaseManager);
        clanManager = new ClanManager(this, clanStorage);
        clanManager.loadClans();

        menuBuilder = new MenuBuilder(this);

        getCommand("onlyclans").setExecutor(new MainCommand(this));
        getCommand("onlyclans").setTabCompleter(new MainCommand(this));

        ChatCommand chatCommand = new ChatCommand(this);
        getCommand("clanchat").setExecutor(chatCommand);
        getCommand("clanchat").setTabCompleter(chatCommand);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(new ClanDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new ClanChatListener(this), this);

        FoliaCompat.runGlobalTimer(this, new org.zkaleejoo.gui.MenuUpdateTask(this), 1L, 1L);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new org.zkaleejoo.hooks.PlaceholderAPIHook(this).register();
        }

        startUpdateChecks();

        Bukkit.getConsoleSender().sendMessage(MessageUtils.toComponent("&9&lOnlyClans &8» &fPlugin Enabled!"));
        Bukkit.getConsoleSender().sendMessage(MessageUtils.toComponent(
                "&9&lOnlyClans &8» &9________         .__         _________ .__                        "));
        Bukkit.getConsoleSender().sendMessage(MessageUtils.toComponent(
                "&9&lOnlyClans &8» &9\\_____  \\   ____ |  | ___.__.\\_   ___ \\|  | _____    ____   ______"));
        Bukkit.getConsoleSender().sendMessage(MessageUtils.toComponent(
                "&9&lOnlyClans &8» &9 /   |   \\ /    \\|  |<   |  |/    \\  \\/|  | \\__  \\  /    \\ /  ___/"));
        Bukkit.getConsoleSender().sendMessage(MessageUtils.toComponent(
                "&9&lOnlyClans &8» &9/    |    \\   |  \\  |_\\___  |\\     \\___|  |__/ __ \\|   |  \\\\___ \\ "));
        Bukkit.getConsoleSender().sendMessage(MessageUtils.toComponent(
                "&9&lOnlyClans &8» &9\\_______  /___|  /____/ ____| \\______  /____(____  /___|  /____  >"));
        Bukkit.getConsoleSender().sendMessage(MessageUtils.toComponent(
                "&9&lOnlyClans &8» &9        \\/     \\/     \\/             \\/          \\/     \\/     \\/ "));
    }

    private void checkUpdates() {
        if (!getMainConfigManager().isUpdateCheckEnabled())
            return;

        new UpdateChecker(this).getVersion(version -> {
            if (this.getPluginMeta().getVersion().equalsIgnoreCase(version)) {
                this.latestVersion = null;
                Bukkit.getConsoleSender().sendMessage(MessageUtils.toComponent(
                        "&5&lOnlyClans &8» &fA check for updates was performed and nothing was found."));
            } else {
                this.latestVersion = version;
                Bukkit.getConsoleSender().sendMessage(MessageUtils.toComponent(
                        "&9&lOnlyClans &8» &f&lNEW VERSION: &7" + version));
                Bukkit.getConsoleSender().sendMessage(
                        MessageUtils.toComponent(
                                "&9&lOnlyClans &8» &fDownload it now at the following link: &7https://modrinth.com/plugin/onlyclans"));
            }
        });
    }

    private void startUpdateChecks() {
        if (updateCheckTask != null) {
            updateCheckTask.cancel();
            updateCheckTask = null;
        }

        if (!getMainConfigManager().isUpdateCheckEnabled()) {
            return;
        }

        checkUpdates();
        updateCheckTask = FoliaCompat.runGlobalTimer(this, this::checkUpdates,
                UPDATE_CHECK_INTERVAL_TICKS, UPDATE_CHECK_INTERVAL_TICKS);
    }

    private void syncMetricsState() {
        if (getMainConfigManager().isBStatsEnabled()) {
            if (metrics == null) {
                metrics = new Metrics(this, BSTATS_PLUGIN_ID);
            }
            return;
        }

        if (metrics != null) {
            metrics.shutdown();
            metrics = null;
        }
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    @Override
    public void onDisable() {
        if (updateCheckTask != null) {
            updateCheckTask.cancel();
            updateCheckTask = null;
        }

        if (metrics != null) {
            metrics.shutdown();
            metrics = null;
        }

        if (databaseManager != null) {
            databaseManager.close();
        }

        Bukkit.getConsoleSender().sendMessage(MessageUtils.toComponent("&9&lOnlyClans &8» &cPlugin Disabled!"));
    }

    public MainConfigManager getMainConfigManager() {
        return mainConfigManager;
    }

    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    public ClanManager getClanManager() {
        return clanManager;
    }

    public MenuBuilder getMenuBuilder() {
        return menuBuilder;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public void reloadPluginState() {
        mainConfigManager.reloadConfig();
        if (placeholderManager != null) {
            placeholderManager.loadConfig(mainConfigManager.getConfigFile());
        }
        syncMetricsState();
        startUpdateChecks();
    }
}

