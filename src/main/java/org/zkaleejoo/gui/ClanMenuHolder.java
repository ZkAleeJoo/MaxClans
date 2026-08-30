package org.zkaleejoo.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import org.zkaleejoo.models.MemberSortType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClanMenuHolder implements InventoryHolder {

    private final String menuId;
    private int page = 0;
    private MemberSortType memberSortType = MemberSortType.ROLE;
    private final Map<Integer, String> clanSlots = new HashMap<>();
    private final Map<Integer, UUID> memberSlots = new HashMap<>();

    public ClanMenuHolder(String menuId) {
        this.menuId = menuId;
        this.page = 0;
        this.memberSortType = MemberSortType.ROLE;
    }

    public ClanMenuHolder(String menuId, int page) {
        this.menuId = menuId;
        this.page = page;
        this.memberSortType = MemberSortType.ROLE;
    }

    public ClanMenuHolder(String menuId, int page, MemberSortType memberSortType) {
        this.menuId = menuId;
        this.page = page;
        this.memberSortType = memberSortType != null ? memberSortType : MemberSortType.ROLE;
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

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }
}
