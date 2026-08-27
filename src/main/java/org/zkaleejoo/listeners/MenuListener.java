package org.zkaleejoo.listeners;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.gui.MenuBuilder;
import org.zkaleejoo.managers.ClanManager;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.utils.MessageUtils;

public class MenuListener implements Listener {

    private final OnlyClans plugin;

    public MenuListener(OnlyClans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        Inventory inventory = event.getInventory();
        String title = PlainTextComponentSerializer.plainText().serialize(
                event.getView().title());

        MenuBuilder menuBuilder = plugin.getMenuBuilder();
        String menuId = menuBuilder.getMenuId(
                MessageUtils.getColoredMessage(
                        PlainTextComponentSerializer.plainText().serialize(event.getView().title())));

        if (menuId == null) {
            menuId = findMenuIdByTitle(title);
        }

        if (menuId == null)
            return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null)
            return;

        int slot = event.getRawSlot();
        String action = menuBuilder.getAction(menuId, slot);

        if (action == null || action.isEmpty())
            return;

        handleAction(player, action);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;

        String title = PlainTextComponentSerializer.plainText().serialize(
                event.getView().title());

        if (findMenuIdByTitle(title) != null) {
            event.setCancelled(true);
        }
    }

    private String findMenuIdByTitle(String plainTitle) {
        var menusConfig = plugin.getMainConfigManager().getMenusConfig();
        var menusSection = menusConfig.getConfigurationSection("menus");
        if (menusSection == null)
            return null;

        for (String menuId : menusSection.getKeys(false)) {
            String rawTitle = menusSection.getString(menuId + ".title", "");
            String stripped = MessageUtils.stripColor(rawTitle);
            if (stripped != null && stripped.equals(plainTitle)) {
                return menuId;
            }
        }
        return null;
    }

    private void handleAction(Player player, String action) {
        ClanManager clanManager = plugin.getClanManager();

        switch (action.toLowerCase()) {
            case "open:create" -> {
                player.closeInventory();
                plugin.getMenuBuilder().openMenu(player, "create", null);
            }
            case "open:info" -> {
                Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
                if (clan != null) {
                    player.closeInventory();
                    plugin.getMenuBuilder().openMenu(player, "info", clan);
                } else {
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("not-in-clan", "&cYou are not in a clan.")));
                }
            }
            case "open:members" -> {
                Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
                if (clan != null) {
                    player.closeInventory();
                    plugin.getMenuBuilder().openMenu(player, "members", clan);
                }
            }
            case "open:settings" -> {
                Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
                if (clan != null) {
                    player.closeInventory();
                    plugin.getMenuBuilder().openMenu(player, "settings", clan);
                }
            }
            case "open:main" -> {
                player.closeInventory();
                Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
                plugin.getMenuBuilder().openMenu(player, "main", clan);
            }
            case "action:create_clan" -> {
                player.closeInventory();
                player.sendMessage(MessageUtils.getColoredMessage(
                        plugin.getMainConfigManager().getPrefix()
                                + plugin.getMainConfigManager().getMessage("type-clan-name",
                                        "&eType the clan name in chat. Type &c'cancel' &eto cancel.")));
                clanManager.addPendingCreation(player.getUniqueId());
            }
            case "action:toggle_ff" -> {
                Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
                if (clan != null && clan.getMember(player.getUniqueId()).isLeader()) {
                    clanManager.toggleFriendlyFire(clan);
                    player.closeInventory();
                    plugin.getMenuBuilder().openMenu(player, "settings", clan);
                }
            }
            case "action:disband" -> {
                Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
                if (clan != null && clan.getMember(player.getUniqueId()).isLeader()) {
                    player.closeInventory();
                    plugin.getMenuBuilder().openMenu(player, "confirm-disband", clan);
                }
            }
            case "action:confirm_disband" -> {
                Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
                if (clan != null && clan.getMember(player.getUniqueId()).isLeader()) {
                    player.closeInventory();
                    clanManager.disbandClan(clan, player);
                }
            }
            case "action:leave" -> {
                Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
                if (clan != null) {
                    player.closeInventory();
                    clanManager.leaveClan(player);
                }
            }
            case "close" -> player.closeInventory();
            default -> {
                if (action.startsWith("open:")) {
                    String targetMenu = action.substring(5);
                    player.closeInventory();
                    Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
                    plugin.getMenuBuilder().openMenu(player, targetMenu, clan);
                }
            }
        }
    }
}
