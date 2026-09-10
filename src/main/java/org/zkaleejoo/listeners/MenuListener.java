package org.zkaleejoo.listeners;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.zkaleejoo.MaxClans;
import org.zkaleejoo.gui.ClanMenuHolder;
import org.zkaleejoo.gui.MenuBuilder;
import org.zkaleejoo.managers.ClanManager;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.models.ClanFlag;
import org.zkaleejoo.models.ClanPlayer;
import org.zkaleejoo.models.ClanRole;
import org.zkaleejoo.models.TopSortType;
import org.zkaleejoo.utils.MessageUtils;
import org.zkaleejoo.utils.SoundUtils;

import java.util.List;

public class MenuListener implements Listener {

    private final MaxClans plugin;

    public MenuListener(MaxClans plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        Inventory topInventory = event.getView().getTopInventory();
        if (!ClanMenuHolder.isClanMenu(topInventory))
            return;

        event.setCancelled(true);
        event.setResult(org.bukkit.event.Event.Result.DENY);

        ClanMenuHolder holder = ClanMenuHolder.getHolder(topInventory);
        if (holder == null)
            return;

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null)
            return;

        if (clickedInventory.equals(event.getView().getBottomInventory()))
            return;

        if (!clickedInventory.equals(topInventory))
            return;

        int slot = event.getSlot();
        if (slot < 0 || slot >= topInventory.getSize())
            return;

        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || currentItem.getType().isAir())
            return;

        String menuId = holder.getMenuId();
        MenuBuilder menuBuilder = plugin.getMenuBuilder();

        String clanAtSlot = holder.getClanAtSlot(slot);
        if (clanAtSlot != null) {
            Clan targetClan = plugin.getClanManager().getClanByName(clanAtSlot);
            if (targetClan != null) {
                if (!player.hasPermission("maxclans.command.request")) {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                    player.sendMessage(MessageUtils.toComponent(
                            plugin.getMainConfigManager().getPrefix()
                                    + plugin.getMainConfigManager().getNoPermission()));
                    return;
                }
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;

        if (ClanMenuHolder.isClanMenu(event.getView().getTopInventory())) {
            event.setCancelled(true);
            event.setResult(org.bukkit.event.Event.Result.DENY);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (ClanMenuHolder.isClanMenu(event.getInventory())) {
            if (event.getPlayer() instanceof Player player) {
                ItemStack cursor = player.getItemOnCursor();
                if (plugin.getMenuBuilder().isClanMenuItem(cursor)) {
                    player.setItemOnCursor(null);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (plugin.getMenuBuilder().isClanMenuItem(event.getItemDrop().getItemStack())) {
            event.getItemDrop().remove();
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (plugin.getMenuBuilder().isClanMenuItem(event.getItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.getMenuBuilder().isClanMenuItem(event.getItemInHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (plugin.getMenuBuilder().isClanMenuItem(item)) {
                event.getInventory().setResult(null);
                return;
            }
        }
    }

    private void handleAction(Player player, String action, ClanMenuHolder holder, boolean hasCustomSound) {
        ClanManager clanManager = plugin.getClanManager();
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());

        String lowerAction = action.toLowerCase();

        switch (lowerAction) {
            case "open:create" -> {
                if (!player.hasPermission("maxclans.command.create")) {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                    player.sendMessage(MessageUtils.toComponent(
                            plugin.getMainConfigManager().getPrefix()
                                    + plugin.getMainConfigManager().getNoPermission()));
                    return;
                }
                if (!hasCustomSound)
                    SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.2f);
                player.closeInventory();
                plugin.getMenuBuilder().openMenu(player, "create", null);
            }
            case "open:clan_list", "open:clans" -> {
                if (!player.hasPermission("maxclans.command.list")) {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                    player.sendMessage(MessageUtils.toComponent(
                            plugin.getMainConfigManager().getPrefix()
                                    + plugin.getMainConfigManager().getNoPermission()));
                    return;
                }
                if (!hasCustomSound)
                    SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.2f);
                player.closeInventory();
                plugin.getMenuBuilder().openMenu(player, "clan_list", clan, 0);
            }
            case "action:clan_page_prev" -> {
                int currentPage = holder != null ? holder.getPage() : 0;
                if (currentPage > 0) {
                    if (!hasCustomSound)
                        SoundUtils.playSound(player, "ITEM_BOOK_PAGE_TURN", 0.8f, 1.2f);
                    plugin.getMenuBuilder().openMenu(player, "clan_list", clan, currentPage - 1);
                } else {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 0.8f, 1.0f);
                }
            }
            case "action:clan_page_next" -> {
                int totalClans = clanManager.getAllClans().size();
                List<Integer> clanSlots = plugin.getMainConfigManager().getMenusConfig()
                        .getIntegerList("menus.clan_list.clan_slots");
                int itemsPerPage = clanSlots.isEmpty() ? 21 : clanSlots.size();
                int maxPages = Math.max(1, (int) Math.ceil((double) totalClans / itemsPerPage));
                int currentPage = holder != null ? holder.getPage() : 0;
                if (currentPage < maxPages - 1) {
                    if (!hasCustomSound)
                        SoundUtils.playSound(player, "ITEM_BOOK_PAGE_TURN", 0.8f, 1.2f);
                    plugin.getMenuBuilder().openMenu(player, "clan_list", clan, currentPage + 1);
                } else {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 0.8f, 1.0f);
                }
            }
            case "action:member_sort_next", "action:cycle_member_sort" -> {
                if (clan != null && holder != null) {
                    holder.setMemberSortType(holder.getMemberSortType().next());
                    holder.setPage(0);
                    if (!hasCustomSound)
                        SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.3f);
                    plugin.getMenuBuilder().updateInventory(player.getOpenInventory().getTopInventory(),
                            holder.getMenuId(), player, clan);
                }
            }
            case "action:member_sort_prev" -> {
                if (clan != null && holder != null) {
                    holder.setMemberSortType(holder.getMemberSortType().previous());
                    holder.setPage(0);
                    if (!hasCustomSound)
                        SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.1f);
                    plugin.getMenuBuilder().updateInventory(player.getOpenInventory().getTopInventory(),
                            holder.getMenuId(), player, clan);
                }
            }
            case "action:member_page_prev" -> {
                int currentPage = holder != null ? holder.getPage() : 0;
                if (currentPage > 0) {
                    if (!hasCustomSound)
                        SoundUtils.playSound(player, "ITEM_BOOK_PAGE_TURN", 0.8f, 1.2f);
                    if (holder != null)
                        holder.setPage(currentPage - 1);
                    plugin.getMenuBuilder().updateInventory(player.getOpenInventory().getTopInventory(),
                            holder != null ? holder.getMenuId() : "members", player, clan);
                } else {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 0.8f, 1.0f);
                }
            }
            case "action:member_page_next" -> {
                int totalMembers = clan != null ? clan.getMemberCount() : 0;
                List<Integer> memberSlots = plugin.getMainConfigManager().getMenusConfig()
                        .getIntegerList("menus.members.member_slots");
                int itemsPerPage = memberSlots.isEmpty() ? 21 : memberSlots.size();
                int maxPages = Math.max(1, (int) Math.ceil((double) totalMembers / itemsPerPage));
                int currentPage = holder != null ? holder.getPage() : 0;
                if (currentPage < maxPages - 1) {
                    if (!hasCustomSound)
                        SoundUtils.playSound(player, "ITEM_BOOK_PAGE_TURN", 0.8f, 1.2f);
                    if (holder != null)
                        holder.setPage(currentPage + 1);
                    plugin.getMenuBuilder().updateInventory(player.getOpenInventory().getTopInventory(),
                            holder != null ? holder.getMenuId() : "members", player, clan);
                } else {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 0.8f, 1.0f);
                }
            }
            case "open:top", "open:clan_top" -> {
                if (!player.hasPermission("maxclans.command.top")) {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                    player.sendMessage(MessageUtils.toComponent(
                            plugin.getMainConfigManager().getPrefix()
                                    + plugin.getMainConfigManager().getNoPermission()));
                    return;
                }
                if (!hasCustomSound)
                    SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.2f);
                player.closeInventory();
                plugin.getMenuBuilder().openTopMenu(player, TopSortType.KDR, 0);
            }
            case "action:top_sort:kdr" -> {
                if (holder != null) {
                    holder.setTopSortType(TopSortType.KDR);
                    holder.setPage(0);
                    if (!hasCustomSound)
                        SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.3f);
                    plugin.getMenuBuilder().updateInventory(player.getOpenInventory().getTopInventory(),
                            holder.getMenuId(), player, clan);
                }
            }
            case "action:top_sort:kills" -> {
                if (holder != null) {
                    holder.setTopSortType(TopSortType.KILLS);
                    holder.setPage(0);
                    if (!hasCustomSound)
                        SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.3f);
                    plugin.getMenuBuilder().updateInventory(player.getOpenInventory().getTopInventory(),
                            holder.getMenuId(), player, clan);
                }
            }
            case "action:top_sort:members" -> {
                if (holder != null) {
                    holder.setTopSortType(TopSortType.MEMBERS);
                    holder.setPage(0);
                    if (!hasCustomSound)
                        SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.3f);
                    plugin.getMenuBuilder().updateInventory(player.getOpenInventory().getTopInventory(),
                            holder.getMenuId(), player, clan);
                }
            }
            case "action:cycle_top_sort" -> {
                if (holder != null) {
                    holder.setTopSortType(holder.getTopSortType().next());
                    holder.setPage(0);
                    if (!hasCustomSound)
                        SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.3f);
                    plugin.getMenuBuilder().updateInventory(player.getOpenInventory().getTopInventory(),
                            holder.getMenuId(), player, clan);
                }
            }
            case "action:top_page_prev" -> {
                int currentPage = holder != null ? holder.getPage() : 0;
                if (currentPage > 0) {
                    if (!hasCustomSound)
                        SoundUtils.playSound(player, "ITEM_BOOK_PAGE_TURN", 0.8f, 1.2f);
                    if (holder != null)
                        holder.setPage(currentPage - 1);
                    plugin.getMenuBuilder().updateInventory(player.getOpenInventory().getTopInventory(),
                            holder != null ? holder.getMenuId() : "top", player, clan);
                } else {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 0.8f, 1.0f);
                }
            }
            case "action:top_page_next" -> {
                TopSortType tst = holder != null ? holder.getTopSortType() : TopSortType.KDR;
                int totalTop = plugin.getClanManager().getTopClans(tst).size();
                List<Integer> topSlots = plugin.getMainConfigManager().getMenusConfig()
                        .getIntegerList("menus." + (holder != null ? holder.getMenuId() : "top") + ".top_slots");
                int itemsPerPage = topSlots.isEmpty() ? 21 : topSlots.size();
                int maxPages = Math.max(1, (int) Math.ceil((double) totalTop / itemsPerPage));
                int currentPage = holder != null ? holder.getPage() : 0;
                if (currentPage < maxPages - 1) {
                    if (!hasCustomSound)
                        SoundUtils.playSound(player, "ITEM_BOOK_PAGE_TURN", 0.8f, 1.2f);
                    if (holder != null)
                        holder.setPage(currentPage + 1);
                    plugin.getMenuBuilder().updateInventory(player.getOpenInventory().getTopInventory(),
                            holder != null ? holder.getMenuId() : "top", player, clan);
                } else {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 0.8f, 1.0f);
                }
            }
            case "open:info" -> {
                if (!player.hasPermission("maxclans.command.info")) {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                    player.sendMessage(MessageUtils.toComponent(
                            plugin.getMainConfigManager().getPrefix()
                                    + plugin.getMainConfigManager().getNoPermission()));
                    return;
                }
                if (clan != null) {
                    if (!hasCustomSound)
                        SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.2f);
                    player.closeInventory();
                    plugin.getMenuBuilder().openMenu(player, "info", clan);
                } else {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                    player.sendMessage(MessageUtils.toComponent(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("not-in-clan", "&cYou are not in a clan.")));
                }
            }
            case "open:members" -> {
                if (!player.hasPermission("maxclans.command.info")) {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                    player.sendMessage(MessageUtils.toComponent(
                            plugin.getMainConfigManager().getPrefix()
                                    + plugin.getMainConfigManager().getNoPermission()));
                    return;
                }
                if (clan != null) {
                    if (!hasCustomSound)
                        SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.2f);
                    player.closeInventory();
                    plugin.getMenuBuilder().openMenu(player, "members", clan);
                } else {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                }
            }
            case "open:settings" -> {
                if (clan != null) {
                    ClanPlayer cp = clan.getMember(player.getUniqueId());
                    boolean canAccess = player.hasPermission("maxclans.admin")
                            || (cp != null && cp.hasRoleAtLeast(ClanRole.MODERATOR));
                    if (canAccess) {
                        if (!hasCustomSound)
                            SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.2f);
                        player.closeInventory();
                        plugin.getMenuBuilder().openMenu(player, "settings", clan);
                    } else {
                        SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                        player.sendMessage(MessageUtils.toComponent(
                                plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                        .getMessage("only-leader-settings",
                                                "&cOnly clan leaders and moderators can access clan settings.")));
                    }
                } else {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                }
            }
            case "open:flags" -> {
                if (clan != null) {
                    ClanPlayer cp = clan.getMember(player.getUniqueId());
                    boolean canAccess = player.hasPermission("maxclans.admin")
                            || (cp != null && cp.hasRoleAtLeast(ClanRole.MODERATOR));
                    if (canAccess) {
                        if (!hasCustomSound)
                            SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.2f);
                        player.closeInventory();
                        plugin.getMenuBuilder().openMenu(player, "flags", clan);
                    } else {
                        SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                        player.sendMessage(MessageUtils.toComponent(
                                plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                        .getMessage("no-flag-permission",
                                                "&cOnly clan leaders and moderators can modify clan flags.")));
                    }
                } else {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                }
            }
            case "open:main" -> {
                if (!player.hasPermission("maxclans.command.main")) {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                    player.sendMessage(MessageUtils.toComponent(
                            plugin.getMainConfigManager().getPrefix()
                                    + plugin.getMainConfigManager().getNoPermission()));
                    return;
                }
                if (!hasCustomSound)
                    SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.0f);
                player.closeInventory();
                plugin.getMenuBuilder().openMenu(player, "main", clan);
            }
            case "action:create_clan" -> {
                if (!player.hasPermission("maxclans.command.create")) {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                    player.sendMessage(MessageUtils.toComponent(
                            plugin.getMainConfigManager().getPrefix()
                                    + plugin.getMainConfigManager().getNoPermission()));
                    return;
                }
                if (clan != null) {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                    player.sendMessage(MessageUtils.toComponent(
                            plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                    .getMessage("already-in-clan", "&cYou are already in a clan.")));
                    return;
                }
                if (!hasCustomSound)
                    SoundUtils.playSound(player, "ENTITY_EXPERIENCE_ORB_PICKUP", 1.0f, 1.2f);
                player.closeInventory();
                player.sendMessage(MessageUtils.toComponent(
                        plugin.getMainConfigManager().getPrefix()
                                + plugin.getMainConfigManager().getMessage("type-clan-name",
                                        "&eType the clan name in chat. Type &c'cancel' &eto cancel.")));
                clanManager.addPendingCreation(player.getUniqueId());
            }
            case "action:toggle_ff" -> {
                if (clan != null) {
                    boolean success = clanManager.toggleFlag(clan, ClanFlag.FRIENDLY_FIRE, player);
                    if (success) {
                        if (!hasCustomSound)
                            SoundUtils.playSound(player, "BLOCK_NOTE_BLOCK_PLING", 1.0f, 1.8f);
                        plugin.getMenuBuilder().updateInventory(player.getOpenInventory().getTopInventory(),
                                holder != null ? holder.getMenuId() : "settings", player, clan);
                    } else {
                        SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                    }
                } else {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                }
            }
            case "action:disband" -> {
                if (!player.hasPermission("maxclans.command.disband")) {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                    player.sendMessage(MessageUtils.toComponent(
                            plugin.getMainConfigManager().getPrefix()
                                    + plugin.getMainConfigManager().getNoPermission()));
                    return;
                }
                if (clan != null && clan.getMember(player.getUniqueId()) != null
                        && clan.getMember(player.getUniqueId()).isLeader()) {
                    if (!hasCustomSound)
                        SoundUtils.playSound(player, "BLOCK_NOTE_BLOCK_BASS", 1.0f, 0.6f);
                    player.closeInventory();
                    plugin.getMenuBuilder().openMenu(player, "confirm-disband", clan);
                } else {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                }
            }
            case "action:confirm_disband" -> {
                if (!player.hasPermission("maxclans.command.disband")) {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                    player.sendMessage(MessageUtils.toComponent(
                            plugin.getMainConfigManager().getPrefix()
                                    + plugin.getMainConfigManager().getNoPermission()));
                    return;
                }
                if (clan != null && clan.getMember(player.getUniqueId()) != null
                        && clan.getMember(player.getUniqueId()).isLeader()) {
                    if (!hasCustomSound)
                        SoundUtils.playSound(player, "ENTITY_GENERIC_EXPLODE", 0.8f, 1.0f);
                    player.closeInventory();
                    clanManager.disbandClan(clan, player);
                } else {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                }
            }
            case "action:leave" -> {
                if (!player.hasPermission("maxclans.command.leave")) {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                    player.sendMessage(MessageUtils.toComponent(
                            plugin.getMainConfigManager().getPrefix()
                                    + plugin.getMainConfigManager().getNoPermission()));
                    return;
                }
                if (clan != null) {
                    if (!hasCustomSound)
                        SoundUtils.playSound(player, "ITEM_ARMOR_EQUIP_GENERIC", 1.0f, 0.8f);
                    player.closeInventory();
                    clanManager.leaveClan(player);
                } else {
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                }
            }
            case "close" -> {
                if (!hasCustomSound)
                    SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 0.8f);
                player.closeInventory();
            }
            default -> {
                if (lowerAction.startsWith("open:")) {
                    String targetMenu = action.substring(5);
                    if (!hasCustomSound)
                        SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.2f);
                    player.closeInventory();
                    plugin.getMenuBuilder().openMenu(player, targetMenu, clan);
                } else if (lowerAction.startsWith("action:toggle_flag:")) {
                    String flagKey = action.substring("action:toggle_flag:".length()).trim();
                    ClanFlag flag = ClanFlag.fromKey(flagKey);
                    if (clan != null && flag != null) {
                        boolean success = clanManager.toggleFlag(clan, flag, player);
                        if (success) {
                            if (!hasCustomSound)
                                SoundUtils.playSound(player, "BLOCK_NOTE_BLOCK_PLING", 1.0f, 1.8f);
                            plugin.getMenuBuilder().updateInventory(player.getOpenInventory().getTopInventory(),
                                    holder != null ? holder.getMenuId() : "flags", player, clan);
                        } else {
                            SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                        }
                    } else {
                        SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                    }
                } else if (lowerAction.startsWith("action:request_clan:")) {
                    if (!player.hasPermission("maxclans.command.request")) {
                        SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                        player.sendMessage(MessageUtils.toComponent(
                                plugin.getMainConfigManager().getPrefix()
                                        + plugin.getMainConfigManager().getNoPermission()));
                        return;
                    }
                    String targetClanName = action.substring("action:request_clan:".length()).trim();
                    Clan target = clanManager.getClanByName(targetClanName);
                    if (target != null) {
                        if (!hasCustomSound)
                            SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.2f);
                        player.closeInventory();
                        clanManager.requestToJoinClan(player, target);
                    }
                } else if (lowerAction.startsWith("command:")) {
                    String cmd = plugin.getMenuBuilder().replacePlaceholders(action.substring(8).trim(), player, clan);
                    if (cmd.startsWith("/"))
                        cmd = cmd.substring(1);
                    player.closeInventory();
                    player.performCommand(cmd);
                } else if (lowerAction.startsWith("console_command:")) {
                    String cmd = plugin.getMenuBuilder().replacePlaceholders(action.substring(16).trim(), player, clan);
                    if (cmd.startsWith("/"))
                        cmd = cmd.substring(1);
                    player.closeInventory();
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            }
        }
    }
}
