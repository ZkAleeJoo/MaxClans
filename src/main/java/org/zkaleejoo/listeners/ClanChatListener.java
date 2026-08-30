package org.zkaleejoo.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.managers.ClanManager;
import org.zkaleejoo.utils.MessageUtils;

public class ClanChatListener implements Listener {

    private final OnlyClans plugin;

    public ClanChatListener(OnlyClans plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        ClanManager clanManager = plugin.getClanManager();

        if (clanManager.hasPendingCreation(player.getUniqueId())) {
            event.setCancelled(true);

            String message = PlainTextComponentSerializer.plainText().serialize(event.message());

            if (message.equalsIgnoreCase("cancel")) {
                clanManager.removePendingCreation(player.getUniqueId());
                player.sendMessage(MessageUtils.getColoredMessage(
                        plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                .getMessage("creation-cancelled", "&cClan creation cancelled.")));
                return;
            }

            if (message.length() < 3 || message.length() > 16) {
                player.sendMessage(MessageUtils.getColoredMessage(
                        plugin.getMainConfigManager().getPrefix()
                                + plugin.getMainConfigManager().getMessage("invalid-length",
                                        "&cClan name must be between 3 and 16 characters.")));
                return;
            }

            if (!message.matches("^[a-zA-Z0-9_]+$")) {
                player.sendMessage(MessageUtils.getColoredMessage(
                        plugin.getMainConfigManager().getPrefix()
                                + plugin.getMainConfigManager().getMessage("invalid-characters",
                                        "&cClan name can only contain letters, numbers, and underscores.")));
                return;
            }

            clanManager.removePendingCreation(player.getUniqueId());

            String tag = message.length() >= 3
                    ? message.substring(0, 3).toUpperCase()
                    : message.toUpperCase();

            org.zkaleejoo.utils.FoliaCompat.runGlobal(plugin, () -> {
                clanManager.createClan(player, message, tag);
            });
        }
    }
}
