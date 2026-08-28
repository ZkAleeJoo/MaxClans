package org.zkaleejoo.gui;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.utils.MessageUtils;

public class MenuUpdateTask implements Runnable {

    private final OnlyClans plugin;
    private int tickCounter = 0;

    public MenuUpdateTask(OnlyClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        tickCounter++;
        FileConfiguration menusConfig = plugin.getMainConfigManager().getMenusConfig();
        if (menusConfig == null) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            InventoryView view = player.getOpenInventory();
            if (view == null || view.getTopInventory() == null) continue;

            String plainTitle = PlainTextComponentSerializer.plainText().serialize(view.title());
            String menuId = findMenuIdByTitle(plainTitle, menusConfig);

            if (menuId != null) {
                int updateInterval = menusConfig.getInt("menus." + menuId + ".update_interval", 0);
                if (updateInterval > 0 && tickCounter % updateInterval == 0) {
                    Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
                    plugin.getMenuBuilder().updateInventory(view.getTopInventory(), menuId, player, clan);
                }
            }
        }
    }

    private String findMenuIdByTitle(String plainTitle, FileConfiguration menusConfig) {
        var menusSection = menusConfig.getConfigurationSection("menus");
        if (menusSection == null) return null;

        for (String menuId : menusSection.getKeys(false)) {
            String rawTitle = menusSection.getString(menuId + ".title", "");
            String stripped = MessageUtils.stripColor(rawTitle);
            if (stripped != null && stripped.equals(plainTitle)) {
                return menuId;
            }
        }
        return null;
    }
}
