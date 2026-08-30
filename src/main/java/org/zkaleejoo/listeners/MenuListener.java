package org.zkaleejoo.listeners;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.gui.ClanMenuHolder;
import org.zkaleejoo.gui.MenuBuilder;
import org.zkaleejoo.managers.ClanManager;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.models.ClanPlayer;
import org.zkaleejoo.utils.MessageUtils;
import org.zkaleejoo.utils.SoundUtils;

import java.util.List;

public class MenuListener implements Listener {

    private final OnlyClans plugin;

    public MenuListener(OnlyClans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        if (!(event.getInventory().getHolder() instanceof ClanMenuHolder holder))
            return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null)
            return;

        int slot = event.getRawSlot();
        String menuId = holder.getMenuId();
        MenuBuilder menuBuilder = plugin.getMenuBuilder();

        String clanAtSlot = holder.getClanAtSlot(slot);
        if (clanAtSlot != null) {
            Clan targetClan = plugin.getClanManager().getClanByName(clanAtSlot);
            if (targetClan != null) {
                SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.2f);
                player.closeInventory();
                plugin.getClanManager().requestToJoinClan(player, targetClan);
                return;
            }
        }

        ConfigurationSection itemConfig = menuBuilder.getItemConfig(menuId, slot);
        String action = menuBuilder.getAction(menuId, slot);

        if (itemConfig != null && itemConfig.contains("sound")) {
            String soundName = itemConfig.getString("sound");
            float volume = (float) itemConfig.getDouble("sound_volume", 1.0);
            float pitch = (float) itemConfig.getDouble("sound_pitch", 1.0);
            SoundUtils.playSound(player, soundName, volume, pitch);
        }

        if (action == null || action.isEmpty())
            return;

        if (event.isRightClick() && (action.equalsIgnoreCase("action:cycle_member_sort")
                || action.equalsIgnoreCase("action:member_sort_next"))) {
            action = "action:member_sort_prev";
        }

        handleAction(player, action, holder, itemConfig != null && itemConfig.contains("sound"));
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;

        if (event.getInventory().getHolder() instanceof ClanMenuHolder) {
            event.setCancelled(true);
        }
    }

    private void handleAction(Player player, String action, ClanMenuHolder holder, boolean hasCustomSound) {
        ClanManager clanManager = plugin.getClanManager();
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());

        String lowerAction = action.toLowerCase();

        switch (lowerAction) {
            case "open:create" -> {
                if (!hasCustomSound) SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.2f);
                player.closeInventory();
                plugin.getMenuBuilder().openMenu(player, "create", null);
            }
            case "open:clan_list", "open:clans" -> {
                if (!hasCustomSound) SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.2f);
                player.closeInventory();
                plugin.getMenuBuilder().openMenu(player, "clan_list", clan, 0);
            }
            case "action:clan_page_prev" -> {
                int currentPage = holder != null ? holder.getPage() : 0;
                if (currentPage > 0) {
                    if (!hasCustomSound) SoundUtils.playSound(player, "ITEM_BOOK_PAGE_TURN", 0.8f, 1.2f);
                    plugin.getMenuBuilder().openMenu(player, "clan_list", clan, currentPage - 1);
                } else {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 0.8f, 1.0f);
                }
            }
            case "action:clan_page_next" -> {
                int totalClans = clanManager.getAllClans().size();
                List<Integer> clanSlots = plugin.getMainConfigManager().getMenusConfig().getIntegerList("menus.clan_list.clan_slots");
                int itemsPerPage = clanSlots.isEmpty() ? 21 : clanSlots.size();
                int maxPages = Math.max(1, (int) Math.ceil((double) totalClans / itemsPerPage));
                int currentPage = holder != null ? holder.getPage() : 0;
                if (currentPage < maxPages - 1) {
                    if (!hasCustomSound) SoundUtils.playSound(player, "ITEM_BOOK_PAGE_TURN", 0.8f, 1.2f);
                    plugin.getMenuBuilder().openMenu(player, "clan_list", clan, currentPage + 1);
                } else {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 0.8f, 1.0f);
                }
            }
            case "action:member_sort_next", "action:cycle_member_sort" -> {
                if (clan != null && holder != null) {
                    holder.setMemberSortType(holder.getMemberSortType().next());
                    holder.setPage(0);
                    if (!hasCustomSound) SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.3f);
                    plugin.getMenuBuilder().updateInventory(player.getOpenInventory().getTopInventory(), holder.getMenuId(), player, clan);
                }
            }
            case "action:member_sort_prev" -> {
                if (clan != null && holder != null) {
                    holder.setMemberSortType(holder.getMemberSortType().previous());
                    holder.setPage(0);
                    if (!hasCustomSound) SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.1f);
                    plugin.getMenuBuilder().updateInventory(player.getOpenInventory().getTopInventory(), holder.getMenuId(), player, clan);
                }
            }
            case "action:member_page_prev" -> {
                int currentPage = holder != null ? holder.getPage() : 0;
                if (currentPage > 0) {
                    if (!hasCustomSound) SoundUtils.playSound(player, "ITEM_BOOK_PAGE_TURN", 0.8f, 1.2f);
                    if (holder != null) holder.setPage(currentPage - 1);
                    plugin.getMenuBuilder().updateInventory(player.getOpenInventory().getTopInventory(), holder != null ? holder.getMenuId() : "members", player, clan);
                } else {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 0.8f, 1.0f);
                }
            }
            case "action:member_page_next" -> {
                int totalMembers = clan != null ? clan.getMemberCount() : 0;
                List<Integer> memberSlots = plugin.getMainConfigManager().getMenusConfig().getIntegerList("menus.members.member_slots");
                int itemsPerPage = memberSlots.isEmpty() ? 21 : memberSlots.size();
                int maxPages = Math.max(1, (int) Math.ceil((double) totalMembers / itemsPerPage));
                int currentPage = holder != null ? holder.getPage() : 0;
                if (currentPage < maxPages - 1) {
                    if (!hasCustomSound) SoundUtils.playSound(player, "ITEM_BOOK_PAGE_TURN", 0.8f, 1.2f);
                    if (holder != null) holder.setPage(currentPage + 1);
                    plugin.getMenuBuilder().updateInventory(player.getOpenInventory().getTopInventory(), holder != null ? holder.getMenuId() : "members", player, clan);
                } else {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 0.8f, 1.0f);
                }
            }
            case "open:info" -> {
                if (clan != null) {
                    if (!hasCustomSound) SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.2f);
                    player.closeInventory();
                    plugin.getMenuBuilder().openMenu(player, "info", clan);
                } else {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("not-in-clan", "&cYou are not in a clan.")));
                }
            }
            case "open:members" -> {
                if (clan != null) {
                    if (!hasCustomSound) SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.2f);
                    player.closeInventory();
                    plugin.getMenuBuilder().openMenu(player, "members", clan);
                } else {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                }
            }
            case "open:settings" -> {
                if (clan != null) {
                    ClanPlayer cp = clan.getMember(player.getUniqueId());
                    if (cp != null && cp.isLeader()) {
                        if (!hasCustomSound) SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.2f);
                        player.closeInventory();
                        plugin.getMenuBuilder().openMenu(player, "settings", clan);
                    } else {
                        SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                        player.sendMessage(MessageUtils.getColoredMessage(
                                plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                        .getMessage("only-leader-settings", "&cOnly the clan leader can access clan settings.")));
                    }
                } else {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                }
            }
            case "open:main" -> {
                if (!hasCustomSound) SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.0f);
                player.closeInventory();
                plugin.getMenuBuilder().openMenu(player, "main", clan);
            }
            case "action:create_clan" -> {
                if (clan != null) {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                    player.sendMessage(MessageUtils.getColoredMessage(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("already-in-clan", "&cYou are already in a clan.")));
                    return;
                }
                if (!hasCustomSound) SoundUtils.playSound(player, "ENTITY_EXPERIENCE_ORB_PICKUP", 1.0f, 1.2f);
                player.closeInventory();
                player.sendMessage(MessageUtils.getColoredMessage(
                        plugin.getMainConfigManager().getPrefix()
                                + plugin.getMainConfigManager().getMessage("type-clan-name",
                                        "&eType the clan name in chat. Type &c'cancel' &eto cancel.")));
                clanManager.addPendingCreation(player.getUniqueId());
            }
            case "action:toggle_ff" -> {
                if (clan != null && clan.getMember(player.getUniqueId()) != null && clan.getMember(player.getUniqueId()).isLeader()) {
                    clanManager.toggleFriendlyFire(clan);
                    if (!hasCustomSound) SoundUtils.playSound(player, "BLOCK_NOTE_BLOCK_PLING", 1.0f, 1.8f);
                    plugin.getMenuBuilder().updateInventory(player.getOpenInventory().getTopInventory(), "settings", player, clan);
                } else {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                }
            }
            case "action:disband" -> {
                if (clan != null && clan.getMember(player.getUniqueId()) != null && clan.getMember(player.getUniqueId()).isLeader()) {
                    if (!hasCustomSound) SoundUtils.playSound(player, "BLOCK_NOTE_BLOCK_BASS", 1.0f, 0.6f);
                    player.closeInventory();
                    plugin.getMenuBuilder().openMenu(player, "confirm-disband", clan);
                } else {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                }
            }
            case "action:confirm_disband" -> {
                if (clan != null && clan.getMember(player.getUniqueId()) != null && clan.getMember(player.getUniqueId()).isLeader()) {
                    if (!hasCustomSound) SoundUtils.playSound(player, "ENTITY_GENERIC_EXPLODE", 0.8f, 1.0f);
                    player.closeInventory();
                    clanManager.disbandClan(clan, player);
                } else {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                }
            }
            case "action:leave" -> {
                if (clan != null) {
                    if (!hasCustomSound) SoundUtils.playSound(player, "ITEM_ARMOR_EQUIP_GENERIC", 1.0f, 0.8f);
                    player.closeInventory();
                    clanManager.leaveClan(player);
                } else {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                }
            }
            case "close" -> {
                if (!hasCustomSound) SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 0.8f);
                player.closeInventory();
            }
            default -> {
                if (lowerAction.startsWith("open:")) {
                    String targetMenu = action.substring(5);
                    if (!hasCustomSound) SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.2f);
                    player.closeInventory();
                    plugin.getMenuBuilder().openMenu(player, targetMenu, clan);
                } else if (lowerAction.startsWith("action:request_clan:")) {
                    String targetClanName = action.substring("action:request_clan:".length()).trim();
                    Clan target = clanManager.getClanByName(targetClanName);
                    if (target != null) {
                        if (!hasCustomSound) SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.2f);
                        player.closeInventory();
                        clanManager.requestToJoinClan(player, target);
                    }
                } else if (lowerAction.startsWith("command:")) {
                    String cmd = plugin.getMenuBuilder().replacePlaceholders(action.substring(8).trim(), player, clan);
                    if (cmd.startsWith("/")) cmd = cmd.substring(1);
                    player.closeInventory();
                    player.performCommand(cmd);
                } else if (lowerAction.startsWith("console_command:")) {
                    String cmd = plugin.getMenuBuilder().replacePlaceholders(action.substring(16).trim(), player, clan);
                    if (cmd.startsWith("/")) cmd = cmd.substring(1);
                    player.closeInventory();
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            }
        }
    }
}
