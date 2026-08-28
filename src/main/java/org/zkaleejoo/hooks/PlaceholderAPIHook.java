package org.zkaleejoo.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.models.ClanPlayer;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final OnlyClans plugin;

    public PlaceholderAPIHook(OnlyClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "onlyclans";
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
        if (player == null) {
            return "";
        }

        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());

        if (params.equalsIgnoreCase("name")) {
            return clan != null ? clan.getName() : "";
        }

        if (params.equalsIgnoreCase("tag")) {
            return clan != null ? clan.getTag() : "";
        }

        if (params.equalsIgnoreCase("tag_formatted")) {
            if (clan == null)
                return "";
            return org.zkaleejoo.utils.MessageUtils.getColoredMessage("&#8727F5[" + clan.getTag() + "]");
        }

        if (params.equalsIgnoreCase("role")) {
            if (clan == null)
                return "";
            ClanPlayer cp = clan.getMember(player.getUniqueId());
            return cp != null ? cp.getRole().name() : "";
        }

        if (params.equalsIgnoreCase("members_count")) {
            return clan != null ? String.valueOf(clan.getMemberCount()) : "0";
        }

        return null;
    }
}
