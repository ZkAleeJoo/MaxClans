package org.zkaleejoo.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.models.MemberSortType;
import org.zkaleejoo.models.TopSortType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public class ClanMenuHolder implements InventoryHolder {

    private final OnlyClans plugin;
    private final String menuId;
    private int page = 0;
    private MemberSortType memberSortType = MemberSortType.ROLE;
    private TopSortType topSortType = TopSortType.KDR;
    private final Map<Integer, String> clanSlots = new HashMap<>();
    private final Map<Integer, UUID> memberSlots = new HashMap<>();
    private Inventory inventory;

    public ClanMenuHolder(OnlyClans plugin, String menuId) {
        this.plugin = plugin;
        this.menuId = menuId;
        this.page = 0;
        this.memberSortType = MemberSortType.ROLE;
    }

    public ClanMenuHolder(OnlyClans plugin, String menuId, int page) {
        this.plugin = plugin;
        this.menuId = menuId;
        this.page = page;
        this.memberSortType = MemberSortType.ROLE;
    }

    public ClanMenuHolder(OnlyClans plugin, String menuId, int page, TopSortType topSortType) {
        this.plugin = plugin;
        this.menuId = menuId;
        this.page = page;
        this.memberSortType = MemberSortType.ROLE;
        this.topSortType = topSortType != null ? topSortType : TopSortType.KDR;
    }

    public ClanMenuHolder(OnlyClans plugin, String menuId, int page, MemberSortType memberSortType) {
        this.plugin = plugin;
        this.menuId = menuId;
        this.page = page;
        this.memberSortType = memberSortType != null ? memberSortType : MemberSortType.ROLE;
        this.topSortType = TopSortType.KDR;
    }

    public ClanMenuHolder(String menuId) {
        this(null, menuId, 0, MemberSortType.ROLE);
    }

    public ClanMenuHolder(String menuId, int page) {
        this(null, menuId, page, MemberSortType.ROLE);
    }

    public ClanMenuHolder(String menuId, int page, MemberSortType memberSortType) {
        this(null, menuId, page, memberSortType);
    }

    public ClanMenuHolder(String menuId, int page, TopSortType topSortType) {
        this(null, menuId, page, topSortType);
    }

    public OnlyClans getPlugin() {
        return plugin;
    }

    public TopSortType getTopSortType() {
        return topSortType;
    }

    public void setTopSortType(TopSortType topSortType) {
        this.topSortType = topSortType != null ? topSortType : TopSortType.KDR;
    }

    public MemberSortType getMemberSortType() {
        return memberSortType;
    }

    public void setMemberSortType(MemberSortType memberSortType) {
        this.memberSortType = memberSortType != null ? memberSortType : MemberSortType.ROLE;
    }

    public String getMenuId() {
        return menuId;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void clearSlots() {
        clanSlots.clear();
        memberSlots.clear();
    }

    public void setClanAtSlot(int slot, String clanName) {
        clanSlots.put(slot, clanName);
    }

    public String getClanAtSlot(int slot) {
        return clanSlots.get(slot);
    }

    public void setMemberAtSlot(int slot, UUID memberUuid) {
        memberSlots.put(slot, memberUuid);
    }

    public UUID getMemberAtSlot(int slot) {
        return memberSlots.get(slot);
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @Nullable Inventory getInventory() {
        return inventory;
    }

    public static boolean isClanMenu(@Nullable Inventory inventory) {
        return inventory != null && inventory.getHolder() instanceof ClanMenuHolder;
    }

    public static boolean isClanMenu(@Nullable InventoryView view) {
        return view != null && isClanMenu(view.getTopInventory());
    }

    public static @Nullable ClanMenuHolder getHolder(@Nullable Inventory inventory) {
        if (inventory != null && inventory.getHolder() instanceof ClanMenuHolder holder) {
            return holder;
        }
        return null;
    }

    public static @Nullable ClanMenuHolder getHolder(@Nullable InventoryView view) {
        if (view == null)
            return null;
        return getHolder(view.getTopInventory());
    }
}
