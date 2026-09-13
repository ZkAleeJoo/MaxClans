package org.zkaleejoo;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
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
import org.zkaleejoo.managers.ClanLevelManager;
import org.zkaleejoo.managers.ClanChestManager;
import org.zkaleejoo.managers.ClanQuestManager;
import org.zkaleejoo.managers.ClanTeleportManager;
import org.zkaleejoo.managers.PlaceholderManager;
import org.zkaleejoo.utils.FoliaCompat;
import org.zkaleejoo.utils.FoliaCompat.WrappedTask;
import org.zkaleejoo.utils.MessageUtils;
import org.zkaleejoo.utils.UpdateChecker;

public class MaxClans extends JavaPlugin {

    private static final int BSTATS_PLUGIN_ID = 33651;
    private static final long UPDATE_CHECK_INTERVAL_TICKS = 20L * 60L * 60L * 5L;

    private MainConfigManager mainConfigManager;
    private PlaceholderManager placeholderManager;
    private DatabaseManager databaseManager;
    private ClanStorage clanStorage;
    private ClanManager clanManager;
    private ClanTeleportManager clanTeleportManager;
    private ClanLevelManager clanLevelManager;
    private ClanChestManager clanChestManager;
    private ClanQuestManager clanQuestManager;
    private MenuBuilder menuBuilder;
    private String latestVersion;
    private Metrics metrics;
    private WrappedTask updateCheckTask;
    private WrappedTask baseEffectTask;

    @Override
    public void onEnable() {
        mainConfigManager = new MainConfigManager(this);
        placeholderManager = new PlaceholderManager(this);
        placeholderManager.loadConfig(mainConfigManager.getConfigFile());
        syncMetricsState();

        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();

        clanStorage = new ClanStorage(this, databaseManager);
        clanLevelManager = new ClanLevelManager(this);
        clanChestManager = new ClanChestManager(this);
        clanQuestManager = new ClanQuestManager(this);

        clanManager = new ClanManager(this, clanStorage);
        clanManager.loadClans();

        clanTeleportManager = new ClanTeleportManager(this);

        menuBuilder = new MenuBuilder(this);

        PluginCommand maxclansCmd = getCommand("maxclans");
        if (maxclansCmd != null) {
            MainCommand mainCommand = new MainCommand(this);
            maxclansCmd.setExecutor(mainCommand);
            maxclansCmd.setTabCompleter(mainCommand);
        } else {
            getLogger().severe("Command 'maxclans' not found in plugin.yml!");
        }

        PluginCommand clanChatCmd = getCommand("clanchat");
        if (clanChatCmd != null) {
            ChatCommand chatCommand = new ChatCommand(this);
            clanChatCmd.setExecutor(chatCommand);
            clanChatCmd.setTabCompleter(chatCommand);
        } else {
            getLogger().severe("Command 'clanchat' not found in plugin.yml!");
        }

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(new ClanDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new ClanChatListener(this), this);
        getServer().getPluginManager().registerEvents(new org.zkaleejoo.listeners.ClanTeleportListener(this), this);
        getServer().getPluginManager().registerEvents(new org.zkaleejoo.listeners.ClanExpListener(this), this);

        FoliaCompat.runGlobalTimer(this, new org.zkaleejoo.gui.MenuUpdateTask(this), 1L, 1L);
        baseEffectTask = FoliaCompat.runGlobalTimer(this, new org.zkaleejoo.tasks.ClanBaseEffectTask(this), 60L, 60L);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new org.zkaleejoo.hooks.PlaceholderAPIHook(this).register();
        }

        startUpdateChecks();

        Bukkit.getConsoleSender().sendMessage(MessageUtils.toComponent("&9&lMaxClans &8» &fPlugin Enabled!"));
        Bukkit.getConsoleSender().sendMessage(MessageUtils.toComponent(
                "&9&lMaxClans &8» &9  __  __              _____ _                 "));
        Bukkit.getConsoleSender().sendMessage(MessageUtils.toComponent(
                "&9&lMaxClans &8» &9 |  \\/  |            / ____| |                "));
        Bukkit.getConsoleSender().sendMessage(MessageUtils.toComponent(
                "&9&lMaxClans &8» &9 | \\  / | __ ___  __| |    | | __ _ _ __  ___ "));
        Bukkit.getConsoleSender().sendMessage(MessageUtils.toComponent(
                "&9&lMaxClans &8» &9 | |\\/| |/ _` \\ \\/ /| |    | |/ _` | '_ \\/ __|"));
        Bukkit.getConsoleSender().sendMessage(MessageUtils.toComponent(
                "&9&lMaxClans &8» &9 | |  | | (_| |>  < | |____| | (_| | | | \\__ \\"));
        Bukkit.getConsoleSender().sendMessage(MessageUtils.toComponent(
                "&9&lMaxClans &8» &9 |_|  |_|\\__,_/_/\\_\\ \\_____|_|\\__,_|_| |_|___/"));
    }

    private void checkUpdates() {
        if (!getMainConfigManager().isUpdateCheckEnabled())
            return;

        new UpdateChecker(this).getVersion(version -> {
            if (this.getPluginMeta().getVersion().equalsIgnoreCase(version)) {
                this.latestVersion = null;
                Bukkit.getConsoleSender().sendMessage(MessageUtils.toComponent(
                        "&5&lMaxClans &8» &fA check for updates was performed and nothing was found."));
            } else {
                this.latestVersion = version;
                Bukkit.getConsoleSender().sendMessage(MessageUtils.toComponent(
                        "&9&lMaxClans &8» &f&lNEW VERSION: &7" + version));
                Bukkit.getConsoleSender().sendMessage(
                        MessageUtils.toComponent(
                                "&9&lMaxClans &8» &fDownload it now at the following link: &7https://modrinth.com/plugin/maxclans"));
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

        if (baseEffectTask != null) {
            baseEffectTask.cancel();
            baseEffectTask = null;
        }

        if (clanChestManager != null) {
            clanChestManager.saveAll();
        }

        if (clanStorage != null) {
            clanStorage.flushAndAwait(3000L);
            clanStorage.close();
        }

        if (clanTeleportManager != null) {
            clanTeleportManager.cancelAll();
            clanTeleportManager = null;
        }

        if (databaseManager != null) {
            databaseManager.close();
        }

        Bukkit.getConsoleSender().sendMessage(MessageUtils.toComponent("&9&lMaxClans &8» &cPlugin Disabled!"));
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

    public ClanTeleportManager getClanTeleportManager() {
        return clanTeleportManager;
    }

    public ClanLevelManager getClanLevelManager() {
        return clanLevelManager;
    }

    public ClanChestManager getClanChestManager() {
        return clanChestManager;
    }

    public ClanQuestManager getClanQuestManager() {
        return clanQuestManager;
    }

    public ClanStorage getClanStorage() {
        return clanStorage;
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
        if (clanLevelManager != null) {
            clanLevelManager.loadConfig();
        }
        if (clanQuestManager != null) {
            clanQuestManager.loadConfig();
        }
        syncMetricsState();
        startUpdateChecks();
    }
}
