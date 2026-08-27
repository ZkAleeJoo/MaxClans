package org.zkaleejoo.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.utils.MessageUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuBuilder {

    private final OnlyClans plugin;

    private final Map<String, String> openMenuTitles = new HashMap<>();

    public MenuBuilder(OnlyClans plugin) {
        this.plugin = plugin;
    }

    /**
     *
     * @param player
     * @param menuId
     * @param clan
     */
    public void openMenu(Player player, String menuId, Clan clan) {
        FileConfiguration menusConfig = plugin.getMainConfigManager().getMenusConfig();
        ConfigurationSection menuSection = menusConfig.getConfigurationSection("menus." + menuId);

        if (menuSection == null) {
            player.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getMainConfigManager().getPrefix() + "&cMenu '" + menuId + "' not found."));
            return;
        }

        String rawTitle = menuSection.getString("title", "&8Menu");
        String title = replacePlaceholders(rawTitle, player, clan);
        int size = menuSection.getInt("size", 27);

        if (size % 9 != 0 || size < 9 || size > 54) {
            size = 27;
        }

        Inventory inventory = Bukkit.createInventory(null, size,
                MessageUtils.toComponent(title));

        String coloredTitle = MessageUtils.getColoredMessage(title);
        openMenuTitles.put(coloredTitle, menuId);

        ConfigurationSection itemsSection = menuSection.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String itemKey : itemsSection.getKeys(false)) {
                ConfigurationSection itemConfig = itemsSection.getConfigurationSection(itemKey);
                if (itemConfig == null)
                    continue;

                int slot = itemConfig.getInt("slot", 0);
                String materialName = itemConfig.getString("material", "STONE");
                String name = itemConfig.getString("name", "");
                List<String> lore = itemConfig.getStringList("lore");
                String action = itemConfig.getString("action", "");

                ItemStack item = createItem(materialName, name, lore, player, clan);

                if (slot >= 0 && slot < size) {
                    inventory.setItem(slot, item);
                }
            }
        }

        String fillerMaterial = menuSection.getString("filler", null);
        if (fillerMaterial != null) {
            Material filler = Material.matchMaterial(fillerMaterial);
            if (filler != null) {
                ItemStack fillerItem = new ItemStack(filler);
                ItemMeta fillerMeta = fillerItem.getItemMeta();
                if (fillerMeta != null) {
                    fillerMeta.displayName(MessageUtils.toComponent(" "));
                    fillerItem.setItemMeta(fillerMeta);
                }
                for (int i = 0; i < size; i++) {
                    if (inventory.getItem(i) == null) {
                        inventory.setItem(i, fillerItem);
                    }
                }
            }
        }

        player.openInventory(inventory);
    }

    private ItemStack createItem(String materialName, String name, List<String> lore,
            Player player, Clan clan) {
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            material = Material.STONE;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return item;

        String processedName = replacePlaceholders(name, player, clan);
        meta.displayName(MessageUtils.legacyToComponentNoItalic(processedName));

        if (lore != null && !lore.isEmpty()) {
            List<net.kyori.adventure.text.Component> loreComponents = new ArrayList<>();
            for (String line : lore) {
                String processedLine = replacePlaceholders(line, player, clan);
                loreComponents.add(MessageUtils.legacyToComponentNoItalic(processedLine));
            }
            meta.lore(loreComponents);
        }

        if (material == Material.PLAYER_HEAD && meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(player);
        }

        item.setItemMeta(meta);
        return item;
    }

    private String replacePlaceholders(String text, Player player, Clan clan) {
        if (text == null)
            return "";

        text = text.replace("{player}", player.getName());

        if (clan != null) {
            text = text.replace("{clan_name}", clan.getName());
            text = text.replace("{clan_tag}", clan.getTag());
            text = text.replace("{clan_members}", String.valueOf(clan.getMemberCount()));
            text = text.replace("{clan_ff}", clan.isFriendlyFire() ? "&aON" : "&cOFF");
        } else {
            text = text.replace("{clan_name}", "None");
            text = text.replace("{clan_tag}", "N/A");
            text = text.replace("{clan_members}", "0");
            text = text.replace("{clan_ff}", "&7N/A");
        }

        return text;
    }

    public String getMenuId(String title) {
        return openMenuTitles.get(title);
    }

    public String getAction(String menuId, int slot) {
        FileConfiguration menusConfig = plugin.getMainConfigManager().getMenusConfig();
        ConfigurationSection itemsSection = menusConfig.getConfigurationSection("menus." + menuId + ".items");

        if (itemsSection == null)
            return null;

        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection item = itemsSection.getConfigurationSection(key);
            if (item != null && item.getInt("slot") == slot) {
                return item.getString("action", null);
            }
        }
        return null;
    }
}
