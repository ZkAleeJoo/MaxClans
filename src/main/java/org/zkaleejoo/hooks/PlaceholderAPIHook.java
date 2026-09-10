package org.zkaleejoo.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.zkaleejoo.MaxClans;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final MaxClans plugin;

    public PlaceholderAPIHook(MaxClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "maxclans";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ZkAleeJoo";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (plugin.getPlaceholderManager() != null) {
            return plugin.getPlaceholderManager().resolvePlaceholder(player, params);
        }
        return null;
    }
}

