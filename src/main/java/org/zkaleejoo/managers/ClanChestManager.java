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
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.utils.MessageUtils;
import org.zkaleejoo.utils.SoundUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClanChestManager implements Listener {

    private final OnlyClans plugin;
    private final Map<String, Inventory> activeInventories = new ConcurrentHashMap<>();

    public static class ClanChestHolder implements InventoryHolder {
        private final String clanName;
        private Inventory inventory;

        public ClanChestHolder(String clanName) {
            this.clanName = clanName;
        }

        public String getClanName() {
            return clanName;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }
    }

    public ClanChestManager(OnlyClans plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openChest(Player player, Clan clan) {
        if (clan == null) return;

        ClanLevelManager levelManager = plugin.getClanLevelManager();
        if (levelManager != null && !levelManager.hasChestAccess(clan.getLevel())) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix()
                            + plugin.getMainConfigManager().getMessage("chest-locked",
                            "&cEl baúl compartido se desbloquea en el &eNivel 3 &cde Clan. Nivel actual: &e{level}")
                            .replace("{level}", String.valueOf(clan.getLevel()))));
            SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
            return;
        }

        String key = clan.getName().toLowerCase();
        int rows = levelManager != null ? levelManager.getChestRows(clan.getLevel()) : 3;
        int size = Math.max(9, Math.min(54, rows * 9));

        Inventory inventory = activeInventories.computeIfAbsent(key, k -> {
            ClanChestHolder holder = new ClanChestHolder(clan.getName());
            String title = plugin.getMainConfigManager().getMessage("chest-title", "&#2F6AFA&lBaúl de Clan &8- &f{clan}")
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

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (top.getHolder() instanceof ClanChestHolder holder) {
            String clanName = holder.getClanName();
            if (clanName == null) return;

            // Save when players are viewing or after close
            String serialized = serializeItems(top.getContents());
            if (serialized != null) {
                plugin.getClanStorage().saveClanChest(clanName, serialized);
            }

            // If no more viewers, we can either keep or remove cache
            if (top.getViewers().size() <= 1) { // Current viewer is about to close
                activeInventories.remove(clanName.toLowerCase());
            }
        }
    }

    public void saveAll() {
        for (Map.Entry<String, Inventory> entry : activeInventories.entrySet()) {
            String clanName = entry.getKey();
            Inventory inv = entry.getValue();
            String serialized = serializeItems(inv.getContents());
            if (serialized != null) {
                plugin.getClanStorage().saveClanChest(clanName, serialized);
            }
        }
        activeInventories.clear();
    }

    private String serializeItems(ItemStack[] items) {
        if (items == null) return "";
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeInt(items.length);
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to serialize clan chest inventory: " + e.getMessage());
            return null;
        }
    }

    private ItemStack[] deserializeItems(String data) {
        if (data == null || data.trim().isEmpty()) return new ItemStack[0];
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            int length = dataInput.readInt();
            ItemStack[] items = new ItemStack[length];
            for (int i = 0; i < length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }
            dataInput.close();
            return items;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to deserialize clan chest inventory: " + e.getMessage());
            return new ItemStack[0];
        }
    }
}
