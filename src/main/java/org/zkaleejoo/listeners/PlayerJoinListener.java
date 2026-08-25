package org.zkaleejoo.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.utils.MessageUtils;
import org.zkaleejoo.config.MainConfigManager;

public class PlayerJoinListener implements Listener {

    private final OnlyClans plugin;

    public PlayerJoinListener(OnlyClans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        MainConfigManager config = plugin.getMainConfigManager();

        if (player.hasPermission("onlyclans.admin")) {
            String latest = plugin.getLatestVersion();
            if (latest != null && !plugin.getPluginMeta().getVersion().equalsIgnoreCase(latest)) {
                player.sendMessage(" ");
                player.sendMessage(MessageUtils.getColoredMessage(
                        config.getPrefix() + config.getMsgUpdateAvailable().replace("{version}", latest)));
                player.sendMessage(MessageUtils.getColoredMessage(
                        config.getPrefix() + config.getMsgUpdateCurrent().replace("{version}", plugin.getPluginMeta().getVersion())));
                player.sendMessage(MessageUtils.getColoredMessage(config.getPrefix() + config.getMsgUpdateDownload()));
                player.sendMessage(" ");
            }
        }
    }
}
