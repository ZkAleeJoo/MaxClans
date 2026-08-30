package org.zkaleejoo.gui;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.utils.FoliaCompat;

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
        if (menusConfig == null)
            return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            FoliaCompat.runForEntity(plugin, player, () -> {
                if (!player.isOnline())
                    return;

                InventoryView view = player.getOpenInventory();
                if (view == null || view.getTopInventory() == null)
                    return;

                if (ClanMenuHolder.isClanMenu(view.getTopInventory())) {
                    ClanMenuHolder holder = ClanMenuHolder.getHolder(view.getTopInventory());
                    if (holder == null)
                        return;

                    String menuId = holder.getMenuId();
                    int updateInterval = menusConfig.getInt("menus." + menuId + ".update_interval", 0);
                    if (updateInterval > 0 && tickCounter % updateInterval == 0) {
                        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
                        plugin.getMenuBuilder().updateInventory(view.getTopInventory(), menuId, player, clan);
                    }
                }
            });
        }
    }
}

