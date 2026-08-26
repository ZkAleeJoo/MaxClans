package org.zkaleejoo;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;
import org.zkaleejoo.commands.MainCommand;
import org.zkaleejoo.config.MainConfigManager;
import org.zkaleejoo.utils.UpdateChecker;
import org.zkaleejoo.listeners.PlayerJoinListener;
import org.zkaleejoo.utils.MessageUtils;
import org.zkaleejoo.utils.FoliaCompat;
import org.zkaleejoo.utils.FoliaCompat.WrappedTask;

public class OnlyClans extends JavaPlugin {

    private static final int BSTATS_PLUGIN_ID = 33651;
    private static final long UPDATE_CHECK_INTERVAL_TICKS = 20L * 60L * 60L * 5L;

    private MainConfigManager mainConfigManager;
    private String latestVersion;
    private Metrics metrics;
    private WrappedTask updateCheckTask;

    @Override
    public void onEnable() {
        mainConfigManager = new MainConfigManager(this);
        syncMetricsState();

        getCommand("onlyclans").setExecutor(new MainCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        startUpdateChecks();

        Bukkit.getConsoleSender().sendMessage(MessageUtils.getColoredMessage("&9&lOnlyClans &8» &fPlugin Enabled!"));
        Bukkit.getConsoleSender().sendMessage(MessageUtils.getColoredMessage(
                "&9&lOnlyClans &8» &9________         .__         _________ .__                        "));
        Bukkit.getConsoleSender().sendMessage(MessageUtils.getColoredMessage(
                "&9&lOnlyClans &8» &9\\_____  \\   ____ |  | ___.__.\\_   ___ \\|  | _____    ____   ______"));
        Bukkit.getConsoleSender().sendMessage(MessageUtils.getColoredMessage(
                "&9&lOnlyClans &8» &9 /   |   \\ /    \\|  |<   |  |/    \\  \\/|  | \\__  \\  /    \\ /  ___/"));
        Bukkit.getConsoleSender().sendMessage(MessageUtils.getColoredMessage(
                "&9&lOnlyClans &8» &9/    |    \\   |  \\  |_\\___  |\\     \\___|  |__/ __ \\|   |  \\\\___ \\ "));
        Bukkit.getConsoleSender().sendMessage(MessageUtils.getColoredMessage(
                "&9&lOnlyClans &8» &9\\_______  /___|  /____/ ____| \\______  /____(____  /___|  /____  >"));
        Bukkit.getConsoleSender().sendMessage(MessageUtils.getColoredMessage(
                "&9&lOnlyClans &8» &9        \\/     \\/     \\/             \\/          \\/     \\/     \\/ "));
    }

    private void checkUpdates() {
        if (!getMainConfigManager().isUpdateCheckEnabled())
            return;

        new UpdateChecker(this).getVersion(version -> {
            if (this.getPluginMeta().getVersion().equalsIgnoreCase(version)) {
                this.latestVersion = null;
                Bukkit.getConsoleSender().sendMessage(MessageUtils.getColoredMessage(
                        "&5&lOnlyClans &8» &fA check for updates was performed and nothing was found."));
            } else {
                this.latestVersion = version;
                Bukkit.getConsoleSender().sendMessage(MessageUtils.getColoredMessage(
                        "&9&lOnlyClans &8» &f&lNEW VERSION: &7" + version));
                Bukkit.getConsoleSender().sendMessage(
                        MessageUtils.getColoredMessage(
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

        Bukkit.getConsoleSender().sendMessage(MessageUtils.getColoredMessage("&5&lOnlyClans &8» &cPlugin Disabled!"));
    }

    public MainConfigManager getMainConfigManager() {
        return mainConfigManager;
    }

    public void reloadPluginState() {
        mainConfigManager.reloadConfig();
        syncMetricsState();
        startUpdateChecks();
    }
}
