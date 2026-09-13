package org.zkaleejoo.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.zkaleejoo.MaxClans;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.utils.FoliaCompat;
import org.zkaleejoo.utils.MessageUtils;
import org.zkaleejoo.utils.SoundUtils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("null")
public class ClanChestManager implements Listener {

    private final MaxClans plugin;
    private final Map<String, Inventory> activeInventories = new ConcurrentHashMap<>();

    public static class ClanChestHolder implements InventoryHolder {
        private final String clanName;
        private Inventory inventory;
        private volatile boolean purged = false;

        public ClanChestHolder(String clanName) {
            this.clanName = clanName;
        }

        public String getClanName() {
            return clanName;
        }

        public boolean isPurged() {
            return purged;
        }

        public void setPurged(boolean purged) {
            this.purged = purged;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }
    }

    public ClanChestManager(MaxClans plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openChest(Player player, Clan clan) {
        if (clan == null)
            return;

        ClanLevelManager levelManager = plugin.getClanLevelManager();
        if (levelManager != null && !levelManager.hasChestAccess(clan.getLevel())) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix()
                            + plugin.getMainConfigManager().getMessage("chest-locked",
                                    "&cThe shared clan chest unlocks at Clan &eLevel 3&c. Current level: &e{level}")
                                    .replace("{level}", String.valueOf(clan.getLevel()))));
            SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
            return;
        }

        String key = clan.getName().toLowerCase();
        int rows = levelManager != null ? levelManager.getChestRows(clan.getLevel()) : 3;
        int size = Math.max(9, Math.min(54, rows * 9));

        Inventory inventory = activeInventories.computeIfAbsent(key, k -> {
            ClanChestHolder holder = new ClanChestHolder(clan.getName());
            String title = plugin.getMainConfigManager()
                    .getMessage("chest-title", "&#7DD3FC&lClan Chest &#94A3B8- &f{clan}")
                    .replace("{clan}", clan.getName());
            Inventory inv = Bukkit.createInventory(holder, size, MessageUtils.toComponent(title));
            holder.setInventory(inv);

            String serialized = plugin.getClanStorage().loadClanChest(clan.getName());
            if (serialized != null && !serialized.trim().isEmpty()) {
                ItemStack[] items = deserializeItems(serialized);
                if (items != null) {
                    for (int i = 0; i < Math.min(items.length, size); i++) {
                        inv.setItem(i, items[i]);
                    }
                }
            }
            return inv;
        });

        SoundUtils.playSound(player, "BLOCK_CHEST_OPEN", 0.9f, 1.0f);
        player.openInventory(inventory);
    }

    public void purgeClanChest(String clanName) {
        if (clanName == null) {
            return;
        }
        Inventory inv = activeInventories.remove(clanName.toLowerCase());
        if (inv != null) {
            if (inv.getHolder() instanceof ClanChestHolder holder) {
                holder.setPurged(true);
            }
            for (org.bukkit.entity.HumanEntity viewer : new ArrayList<>(inv.getViewers())) {
                FoliaCompat.runForEntity(plugin, viewer, viewer::closeInventory);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (top.getHolder() instanceof ClanChestHolder holder) {
            if (holder.isPurged()) {
                return;
            }

            String clanName = holder.getClanName();
            if (clanName == null)
                return;

            if (plugin.getClanManager() != null && plugin.getClanManager().getClanByName(clanName) == null) {
                activeInventories.remove(clanName.toLowerCase());
                return;
            }

            String serialized = serializeItems(top.getContents());
            if (serialized != null) {
                if (!plugin.isEnabled()) {
                    plugin.getClanStorage().saveClanChestSync(clanName, serialized);
                } else {
                    plugin.getClanStorage().saveClanChest(clanName, serialized);
                }
            }

            if (top.getViewers().size() <= 1) {
                activeInventories.remove(clanName.toLowerCase());
            }
        }
    }

    public void saveAll() {
        Map<String, Inventory> snapshot = new HashMap<>(activeInventories);

        for (Inventory inv : snapshot.values()) {
            for (org.bukkit.entity.HumanEntity viewer : new ArrayList<>(inv.getViewers())) {
                try {
                    viewer.closeInventory();
                } catch (Throwable ignored) {
                }
            }
        }

        for (Map.Entry<String, Inventory> entry : snapshot.entrySet()) {
            Inventory inv = entry.getValue();
            String clanName = entry.getKey();
            if (inv.getHolder() instanceof ClanChestHolder holder) {
                if (holder.isPurged()) {
                    continue;
                }
                if (holder.getClanName() != null) {
                    clanName = holder.getClanName();
                }
            }
            if (plugin.getClanManager() != null && plugin.getClanManager().getClanByName(clanName) == null) {
                continue;
            }
            String serialized = serializeItems(inv.getContents());
            if (serialized != null) {
                plugin.getClanStorage().saveClanChestSync(clanName, serialized);
            }
        }
        activeInventories.clear();
    }

    private String serializeItems(ItemStack[] items) {
        if (items == null)
            return "";
        try {
            byte[] bytes = ItemStack.serializeItemsAsBytes(items);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to serialize clan chest inventory: " + e.getMessage());
            return null;
        }
    }

    private ItemStack[] deserializeItems(String data) {
        if (data == null || data.trim().isEmpty())
            return new ItemStack[0];
        try {
            byte[] bytes = Base64.getMimeDecoder().decode(data);
            return deserializeBytes(bytes);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to deserialize clan chest inventory: " + e.getMessage());
            return new ItemStack[0];
        }
    }

    private ItemStack[] deserializeBytes(byte[] bytes) throws Exception {
        if (bytes.length >= 2 && bytes[0] == (byte) 0xAC && bytes[1] == (byte) 0xED) {
            return deserializeLegacyItems(bytes);
        }
        try {
            return ItemStack.deserializeItemsFromBytes(bytes);
        } catch (Exception e) {
            return deserializeLegacyItems(bytes);
        }
    }

    @SuppressWarnings("deprecation")
    private ItemStack[] deserializeLegacyItems(byte[] bytes) throws Exception {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            int length = dataInput.readInt();
            ItemStack[] items = new ItemStack[length];
            for (int i = 0; i < length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }
            return items;
        }
    }
}
