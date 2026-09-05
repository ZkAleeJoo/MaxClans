package org.zkaleejoo.gui;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.models.ClanFlag;
import org.zkaleejoo.models.ClanPlayer;
import org.zkaleejoo.models.ClanRole;
import org.zkaleejoo.models.MemberSortType;
import org.zkaleejoo.models.TopSortType;
import org.zkaleejoo.utils.MessageUtils;
import org.zkaleejoo.utils.SoundUtils;

import java.util.*;

@SuppressWarnings("unused")
public class MenuBuilder {

    private final OnlyClans plugin;
    private final NamespacedKey menuItemKey;
    private final NamespacedKey menuIdKey;

    public MenuBuilder(OnlyClans plugin) {
        this.plugin = plugin;
        this.menuItemKey = new NamespacedKey(plugin, "gui_item");
        this.menuIdKey = new NamespacedKey(plugin, "gui_menu_id");
    }

    public List<Clan> getVisibleClansFor(Player player) {
        List<Clan> result = new ArrayList<>();
        Clan playerClan = player != null ? plugin.getClanManager().getClanByPlayer(player.getUniqueId()) : null;
        boolean isAdmin = player != null && player.hasPermission("onlyclans.admin");

        for (Clan c : plugin.getClanManager().getAllClans()) {
            if (c.isVisibleInList() || isAdmin || (playerClan != null && playerClan.getName().equalsIgnoreCase(c.getName()))) {
                result.add(c);
            }
        }
        result.sort((a, b) -> Integer.compare(b.getMemberCount(), a.getMemberCount()));
        return result;
    }

    public NamespacedKey getMenuItemKey() {
        return menuItemKey;
    }

    public NamespacedKey getMenuIdKey() {
        return menuIdKey;
    }

    public void markAsMenuItem(ItemMeta meta, String menuId) {
        if (meta == null)
            return;
        meta.getPersistentDataContainer().set(menuItemKey, PersistentDataType.BYTE, (byte) 1);
        if (menuId != null) {
            meta.getPersistentDataContainer().set(menuIdKey, PersistentDataType.STRING, menuId);
        }
    }

    public boolean isClanMenuItem(@Nullable ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(menuItemKey, PersistentDataType.BYTE);
    }

    public void openMenu(Player player, String menuId, Clan clan) {
        openMenu(player, menuId, clan, 0, MemberSortType.ROLE);
    }

    public void openMenu(Player player, String menuId, Clan clan, int page) {
        openMenu(player, menuId, clan, page, MemberSortType.ROLE);
    }

    public void openMenu(Player player, String menuId, Clan clan, int page, MemberSortType sortType) {
        openMenu(player, menuId, clan, page, sortType, TopSortType.KDR);
    }

    public void openTopMenu(Player player, TopSortType sortType, int page) {
        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        openMenu(player, "top", clan, page, MemberSortType.ROLE, sortType != null ? sortType : TopSortType.KDR);
    }

    public void openMenu(Player player, String menuId, Clan clan, int page, MemberSortType sortType, TopSortType topSortType) {
        FileConfiguration menusConfig = plugin.getMainConfigManager().getMenusConfig();
        ConfigurationSection menuSection = menusConfig.getConfigurationSection("menus." + menuId);

        if (menuSection == null) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix()
                            + plugin.getMainConfigManager().getMessage("menu-not-found", "&cMenu '{menu}' not found.")
                                    .replace("{menu}", menuId)));
            SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 1.0f);
            return;
        }

        int maxPages = 1;
        if (menuId.equalsIgnoreCase("clan_list") || menuSection.getBoolean("dynamic_clans", false)) {
            List<Integer> clanSlots = menuSection.getIntegerList("clan_slots");
            int itemsPerPage = clanSlots.isEmpty() ? 21 : clanSlots.size();
            int totalClans = getVisibleClansFor(player).size();
            maxPages = Math.max(1, (int) Math.ceil((double) totalClans / itemsPerPage));
            page = Math.min(Math.max(0, page), maxPages - 1);
        } else if (menuId.equalsIgnoreCase("members") || menuSection.getBoolean("dynamic_members", false)) {
            List<Integer> memberSlots = menuSection.getIntegerList("member_slots");
            int itemsPerPage = memberSlots.isEmpty() ? 21 : memberSlots.size();
            int totalMembers = clan != null ? clan.getMemberCount() : 0;
            maxPages = Math.max(1, (int) Math.ceil((double) totalMembers / itemsPerPage));
            page = Math.min(Math.max(0, page), maxPages - 1);
        } else if (menuId.equalsIgnoreCase("top") || menuId.equalsIgnoreCase("clan_top") || menuSection.getBoolean("dynamic_top", false)) {
            List<Integer> topSlots = menuSection.getIntegerList("top_slots");
            int itemsPerPage = topSlots.isEmpty() ? 21 : topSlots.size();
            int totalTop = plugin.getClanManager().getTopClans(topSortType != null ? topSortType : TopSortType.KDR).size();
            maxPages = Math.max(1, (int) Math.ceil((double) totalTop / itemsPerPage));
            page = Math.min(Math.max(0, page), maxPages - 1);
        }

        ClanMenuHolder holder = new ClanMenuHolder(plugin, menuId, page, sortType != null ? sortType : MemberSortType.ROLE);
        if (topSortType != null) {
            holder.setTopSortType(topSortType);
        }
        String rawTitle = menuSection.getString("title", "&8Menu");
        String title = replacePlaceholders(rawTitle, player, clan, page, holder);
        int size = menuSection.getInt("size", 27);

        if (size % 9 != 0 || size < 9 || size > 54) {
            size = 27;
        }

        Inventory inventory = Bukkit.createInventory(holder, size, MessageUtils.toComponent(title));
        holder.setInventory(inventory);

        updateInventory(inventory, menuId, player, clan);

        String openSound = menuSection.getString("open_sound", "BLOCK_CHEST_OPEN");
        if (openSound != null && !openSound.equalsIgnoreCase("none")) {
            float volume = (float) menuSection.getDouble("open_sound_volume", 0.8);
            float pitch = (float) menuSection.getDouble("open_sound_pitch", 1.1);
            SoundUtils.playSound(player, openSound, volume, pitch);
        }

        player.openInventory(inventory);
    }

    public void updateInventory(Inventory inventory, String menuId, Player player, Clan clan) {
        FileConfiguration menusConfig = plugin.getMainConfigManager().getMenusConfig();
        ConfigurationSection menuSection = menusConfig.getConfigurationSection("menus." + menuId);
        if (menuSection == null)
            return;

        int size = inventory.getSize();
        int page = 0;
        ClanMenuHolder holder = null;
        if (inventory.getHolder() instanceof ClanMenuHolder cmh) {
            holder = cmh;
            page = cmh.getPage();
            holder.clearSlots();
        }

        boolean isClanList = menuId.equalsIgnoreCase("clan_list") || menuSection.getBoolean("dynamic_clans", false);
        boolean isMemberList = menuId.equalsIgnoreCase("members") || menuSection.getBoolean("dynamic_members", false);
        boolean isTop = menuId.equalsIgnoreCase("top") || menuId.equalsIgnoreCase("clan_top") || menuSection.getBoolean("dynamic_top", false);

        int maxPages = 1;
        if (isClanList) {
            List<Integer> clanSlots = menuSection.getIntegerList("clan_slots");
            int itemsPerPage = clanSlots.isEmpty() ? 21 : clanSlots.size();
            int totalClans = getVisibleClansFor(player).size();
            maxPages = Math.max(1, (int) Math.ceil((double) totalClans / itemsPerPage));
            page = Math.min(Math.max(0, page), maxPages - 1);
            if (holder != null) {
                holder.setPage(page);
            }
        } else if (isMemberList) {
            List<Integer> memberSlots = menuSection.getIntegerList("member_slots");
            int itemsPerPage = memberSlots.isEmpty() ? 21 : memberSlots.size();
            int totalMembers = clan != null ? clan.getMemberCount() : 0;
            maxPages = Math.max(1, (int) Math.ceil((double) totalMembers / itemsPerPage));
            page = Math.min(Math.max(0, page), maxPages - 1);
            if (holder != null) {
                holder.setPage(page);
            }
        } else if (isTop) {
            List<Integer> topSlots = menuSection.getIntegerList("top_slots");
            int itemsPerPage = topSlots.isEmpty() ? 21 : topSlots.size();
            TopSortType tst = holder != null ? holder.getTopSortType() : TopSortType.KDR;
            int totalTop = plugin.getClanManager().getTopClans(tst).size();
            maxPages = Math.max(1, (int) Math.ceil((double) totalTop / itemsPerPage));
            page = Math.min(Math.max(0, page), maxPages - 1);
            if (holder != null) {
                holder.setPage(page);
            }
        }

        ConfigurationSection itemsSection = menuSection.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String itemKey : itemsSection.getKeys(false)) {
                ConfigurationSection itemConfig = itemsSection.getConfigurationSection(itemKey);
                if (itemConfig == null)
                    continue;

                List<Integer> slots = getSlotsFromConfig(itemConfig);

                if (isClanList || isMemberList || isTop) {
                    if (itemKey.equalsIgnoreCase("btn_prev") && page <= 0) {
                        for (int slot : slots) {
                            if (slot >= 0 && slot < size) {
                                inventory.setItem(slot, null);
                            }
                        }
                        continue;
                    }
                    if (itemKey.equalsIgnoreCase("btn_next") && page >= maxPages - 1) {
                        for (int slot : slots) {
                            if (slot >= 0 && slot < size) {
                                inventory.setItem(slot, null);
                            }
                        }
                        continue;
                    }
                }

                ItemStack item = createItem(itemConfig, player, clan, page, holder, menuId);

                for (int slot : slots) {
                    if (slot >= 0 && slot < size) {
                        inventory.setItem(slot, item);
                    }
                }
            }
        }

        if (isMemberList) {
            renderDynamicMembers(inventory, menuSection, player, clan, holder);
        }

        if (isClanList) {
            if (holder != null) {
                renderDynamicClans(inventory, menuSection, player, holder);
            }
        }

        if (isTop) {
            if (holder != null) {
                renderDynamicTop(inventory, menuSection, player, holder);
            }
        }

        String fillerMaterial = menuSection.getString("filler", null);
        if (fillerMaterial != null && !fillerMaterial.equalsIgnoreCase("none")) {
            Material filler = Material.matchMaterial(fillerMaterial);
            if (filler != null) {
                ItemStack fillerItem = new ItemStack(filler);
                ItemMeta fillerMeta = fillerItem.getItemMeta();
                if (fillerMeta != null) {
                    fillerMeta.displayName(MessageUtils.toComponent(" "));
                    if (menuSection.getBoolean("filler_glow", false)) {
                        fillerMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
                        fillerMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    }
                    markAsMenuItem(fillerMeta, menuId);
                    fillerItem.setItemMeta(fillerMeta);
                }
                for (int i = 0; i < size; i++) {
                    if (inventory.getItem(i) == null) {
                        inventory.setItem(i, fillerItem);
                    }
                }
            }
        }
    }

    private int getSafeStatistic(OfflinePlayer player, org.bukkit.Statistic statistic) {
        if (player == null)
            return 0;
        try {
            return player.getStatistic(statistic);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private void renderDynamicMembers(Inventory inventory, ConfigurationSection menuSection, Player viewer, Clan clan,
            ClanMenuHolder holder) {
        if (clan == null)
            return;

        List<Integer> memberSlots = menuSection.getIntegerList("member_slots");
        if (memberSlots.isEmpty()) {
            int size = inventory.getSize();
            memberSlots = new ArrayList<>();
            if (size >= 45) {
                for (int i = 10; i <= 16; i++)
                    memberSlots.add(i);
                for (int i = 19; i <= 25; i++)
                    memberSlots.add(i);
                for (int i = 28; i <= 34; i++)
                    memberSlots.add(i);
            } else if (size >= 36) {
                for (int i = 10; i <= 16; i++)
                    memberSlots.add(i);
                for (int i = 19; i <= 25; i++)
                    memberSlots.add(i);
            } else {
                for (int i = 10; i <= 16; i++)
                    memberSlots.add(i);
            }
        }

        MemberSortType sortType = holder != null ? holder.getMemberSortType() : MemberSortType.ROLE;
        List<ClanPlayer> sortedMembers = new ArrayList<>(clan.getMembers().values());

        switch (sortType) {
            case ROLE -> sortedMembers.sort((a, b) -> {
                int cmp = Integer.compare(b.getRole().getWeight(), a.getRole().getWeight());
                if (cmp != 0)
                    return cmp;
                return Long.compare(a.getJoinedAt(), b.getJoinedAt());
            });
            case KDR -> sortedMembers.sort((a, b) -> {
                OfflinePlayer opA = Bukkit.getOfflinePlayer(a.getUuid());
                OfflinePlayer opB = Bukkit.getOfflinePlayer(b.getUuid());
                int killsA = getSafeStatistic(opA, org.bukkit.Statistic.PLAYER_KILLS);
                int deathsA = getSafeStatistic(opA, org.bukkit.Statistic.DEATHS);
                int killsB = getSafeStatistic(opB, org.bukkit.Statistic.PLAYER_KILLS);
                int deathsB = getSafeStatistic(opB, org.bukkit.Statistic.DEATHS);
                double kdrA = (deathsA <= 0) ? (double) killsA : (double) killsA / deathsA;
                double kdrB = (deathsB <= 0) ? (double) killsB : (double) killsB / deathsB;
                int cmp = Double.compare(kdrB, kdrA);
                if (cmp != 0)
                    return cmp;
                return Integer.compare(killsB, killsA);
            });
            case PLAYTIME -> sortedMembers.sort((a, b) -> {
                OfflinePlayer opA = Bukkit.getOfflinePlayer(a.getUuid());
                OfflinePlayer opB = Bukkit.getOfflinePlayer(b.getUuid());
                long ptA = getSafeStatistic(opA, org.bukkit.Statistic.PLAY_ONE_MINUTE);
                long ptB = getSafeStatistic(opB, org.bukkit.Statistic.PLAY_ONE_MINUTE);
                return Long.compare(ptB, ptA);
            });
            case JOIN_RECENT -> sortedMembers.sort((a, b) -> Long.compare(b.getJoinedAt(), a.getJoinedAt()));
            case JOIN_OLDEST -> sortedMembers.sort((a, b) -> Long.compare(a.getJoinedAt(), b.getJoinedAt()));
        }

        int itemsPerPage = memberSlots.size();
        int totalMembers = sortedMembers.size();
        int maxPages = Math.max(1, (int) Math.ceil((double) totalMembers / itemsPerPage));
        int page = holder != null ? holder.getPage() : 0;
        page = Math.min(Math.max(0, page), maxPages - 1);
        if (holder != null) {
            holder.setPage(page);
        }

        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, totalMembers);
        int slotIndex = 0;

        String lang = plugin.getMainConfigManager().getSelectedLanguage();
        ConfigurationSection memberItemConfig = menuSection.getConfigurationSection("member_item");

        for (int slot : memberSlots) {
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, null);
            }
        }

        for (int i = startIndex; i < endIndex; i++) {
            if (slotIndex >= memberSlots.size())
                break;

            ClanPlayer cp = sortedMembers.get(i);
            int targetSlot = memberSlots.get(slotIndex++);
            if (holder != null) {
                holder.setMemberAtSlot(targetSlot, cp.getUuid());
            }

            Player onlinePlayer = Bukkit.getPlayer(cp.getUuid());
            OfflinePlayer offPlayer = onlinePlayer != null ? onlinePlayer : Bukkit.getOfflinePlayer(cp.getUuid());
            String memberName = onlinePlayer != null ? onlinePlayer.getName()
                    : (offPlayer.getName() != null ? offPlayer.getName()
                            : plugin.getPlaceholderManager().getUnknownText(lang));
            boolean isOnline = onlinePlayer != null && onlinePlayer.isOnline();

            int kills = getSafeStatistic(offPlayer, org.bukkit.Statistic.PLAYER_KILLS);
            int deaths = getSafeStatistic(offPlayer, org.bukkit.Statistic.DEATHS);
            String kdr = plugin.getPlaceholderManager().formatKDR(kills, deaths);
            long playtimeTicks = getSafeStatistic(offPlayer, org.bukkit.Statistic.PLAY_ONE_MINUTE);
            String playtimeFormatted = plugin.getPlaceholderManager().formatPlaytime(playtimeTicks, lang);
            long joinedAt = cp.getJoinedAt() > 0 ? cp.getJoinedAt() : clan.getCreatedAt();
            String joinedFormatted = plugin.getPlaceholderManager().formatDate(joinedAt);

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            if (skullMeta != null) {
                markAsMenuItem(skullMeta, "members");
                if (onlinePlayer != null) {
                    skullMeta.setPlayerProfile(onlinePlayer.getPlayerProfile());
                } else {
                    skullMeta.setOwningPlayer(offPlayer);
                }

                String roleBadge = plugin.getPlaceholderManager().getRoleFormatted(cp.getRole(), lang, true);
                String rawRole = plugin.getPlaceholderManager().getRoleName(cp.getRole(), lang);

                String statusLabel = plugin.getPlaceholderManager().getGuiText("status-label", lang,
                        "&#A0AEC0Status: ");
                String statusText = isOnline
                        ? plugin.getPlaceholderManager().getGuiText("status-online", lang, "&#00FF88● Online")
                        : plugin.getPlaceholderManager().getGuiText("status-offline", lang, "&#FF3366○ Offline");
                String roleLabel = plugin.getPlaceholderManager().getGuiText("role-label", lang, "&#A0AEC0Role: ");
                String kdrLabel = plugin.getPlaceholderManager().getGuiText("kdr-label", lang, "&#A0AEC0KDR: ");
                String playtimeLabel = plugin.getPlaceholderManager().getGuiText("playtime-label", lang,
                        "&#A0AEC0Playtime: ");
                String joinedLabel = plugin.getPlaceholderManager().getGuiText("joined-label", lang,
                        "&#A0AEC0Joined: ");
                String managementTitle = plugin.getPlaceholderManager().getGuiText("management-title", lang,
                        "&#FFD700⚡ Management Commands:");

                if (memberItemConfig != null) {
                    String rawTitle = memberItemConfig.getString("name", "%member_role_formatted% &#FFFFFF%member_name%");
                    String processedTitle = rawTitle.replace("%member_name%", memberName)
                            .replace("%member_role%", rawRole)
                            .replace("%member_role_formatted%", roleBadge)
                            .replace("%member_kdr%", kdr)
                            .replace("%member_kills%", String.valueOf(kills))
                            .replace("%member_deaths%", String.valueOf(deaths))
                            .replace("%member_playtime%", playtimeFormatted)
                            .replace("%member_joined%", joinedFormatted);
                    skullMeta.displayName(MessageUtils.toComponentNoItalic(processedTitle));

                    List<String> rawLore = memberItemConfig.getStringList("lore");
                    List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
                    for (String line : rawLore) {
                        String processedLine = line.replace("%member_name%", memberName)
                                .replace("%member_role%", rawRole)
                                .replace("%member_role_formatted%", roleBadge)
                                .replace("%member_status%", statusText)
                                .replace("%member_kdr%", kdr)
                                .replace("%member_kills%", String.valueOf(kills))
                                .replace("%member_deaths%", String.valueOf(deaths))
                                .replace("%member_playtime%", playtimeFormatted)
                                .replace("%member_joined%", joinedFormatted);
                        lore.add(MessageUtils.toComponentNoItalic(processedLine));
                    }

                    ClanPlayer viewerCp = clan.getMember(viewer.getUniqueId());
                    if (viewerCp != null && viewerCp.isLeader() && !cp.getUuid().equals(viewer.getUniqueId())) {
                        lore.add(MessageUtils.toComponentNoItalic("&#718096━━━━━━━━━━━━━━━━━━━━━"));
                        lore.add(MessageUtils.toComponentNoItalic(managementTitle));
                        lore.add(MessageUtils.toComponentNoItalic("&#718096• &#00E5FF/clan promote " + memberName));
                        lore.add(MessageUtils.toComponentNoItalic("&#718096• &#FFAA00/clan demote " + memberName));
                        lore.add(MessageUtils.toComponentNoItalic("&#718096• &#FF3366/clan kick " + memberName));
                        lore.add(MessageUtils.toComponentNoItalic("&#718096━━━━━━━━━━━━━━━━━━━━━"));
                    }
                    skullMeta.lore(lore);
                } else {
                    String title = roleBadge + " &#FFFFFF" + memberName;
                    skullMeta.displayName(MessageUtils.toComponentNoItalic(title));

                    List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
                    lore.add(MessageUtils.toComponentNoItalic("&#718096━━━━━━━━━━━━━━━━━━━━━"));
                    lore.add(MessageUtils.toComponentNoItalic(statusLabel + statusText));
                    lore.add(MessageUtils.toComponentNoItalic(roleLabel + roleBadge));
                    lore.add(MessageUtils.toComponentNoItalic(kdrLabel + "&#00FF88" + kdr + " &#718096(&#FFFFFF"
                            + kills + " &#718096K / &#FFFFFF" + deaths + " &#718096D)"));
                    lore.add(MessageUtils.toComponentNoItalic(playtimeLabel + "&#00E5FF" + playtimeFormatted));
                    lore.add(MessageUtils.toComponentNoItalic(joinedLabel + "&#E2E8F0" + joinedFormatted));
                    lore.add(MessageUtils.toComponentNoItalic("&#718096━━━━━━━━━━━━━━━━━━━━━"));

                    ClanPlayer viewerCp = clan.getMember(viewer.getUniqueId());
                    if (viewerCp != null && viewerCp.isLeader() && !cp.getUuid().equals(viewer.getUniqueId())) {
                        lore.add(MessageUtils.toComponentNoItalic(managementTitle));
                        lore.add(MessageUtils.toComponentNoItalic("&#718096• &#00E5FF/clan promote " + memberName));
                        lore.add(MessageUtils.toComponentNoItalic("&#718096• &#FFAA00/clan demote " + memberName));
                        lore.add(MessageUtils.toComponentNoItalic("&#718096• &#FF3366/clan kick " + memberName));
                        lore.add(MessageUtils.toComponentNoItalic("&#718096━━━━━━━━━━━━━━━━━━━━━"));
                    }

                    skullMeta.lore(lore);
                }

                head.setItemMeta(skullMeta);
            }

            inventory.setItem(targetSlot, head);
        }
    }

    private void renderDynamicClans(Inventory inventory, ConfigurationSection menuSection, Player viewer,
            ClanMenuHolder holder) {
        List<Integer> clanSlots = menuSection.getIntegerList("clan_slots");
        if (clanSlots.isEmpty()) {
            int size = inventory.getSize();
            clanSlots = new ArrayList<>();
            if (size >= 54) {
                for (int i = 10; i <= 16; i++)
                    clanSlots.add(i);
                for (int i = 19; i <= 25; i++)
                    clanSlots.add(i);
                for (int i = 28; i <= 34; i++)
                    clanSlots.add(i);
            } else if (size >= 45) {
                for (int i = 10; i <= 16; i++)
                    clanSlots.add(i);
                for (int i = 19; i <= 25; i++)
                    clanSlots.add(i);
            } else {
                for (int i = 10; i <= 16; i++)
                    clanSlots.add(i);
            }
        }

        List<Clan> allClans = getVisibleClansFor(viewer);

        int itemsPerPage = clanSlots.size();
        int totalClans = allClans.size();
        int maxPages = Math.max(1, (int) Math.ceil((double) totalClans / itemsPerPage));
        int page = Math.min(Math.max(0, holder.getPage()), maxPages - 1);
        holder.setPage(page);

        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, totalClans);
        int slotIndex = 0;

        String lang = plugin.getMainConfigManager().getSelectedLanguage();
        boolean viewerInClan = plugin.getClanManager().isInClan(viewer.getUniqueId());
        Clan viewerClan = plugin.getClanManager().getClanByPlayer(viewer.getUniqueId());

        ConfigurationSection itemTemplate = menuSection.getConfigurationSection("clan_item");

        for (int slot : clanSlots) {
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, null);
            }
        }

        for (int i = startIndex; i < endIndex; i++) {
            if (slotIndex >= clanSlots.size())
                break;

            Clan clan = allClans.get(i);
            int targetSlot = clanSlots.get(slotIndex++);
            holder.setClanAtSlot(targetSlot, clan.getName());

            boolean hasStaff = plugin.getClanManager().hasOnlineStaff(clan);
            boolean alreadyRequested = clan.hasJoinRequest(viewer.getUniqueId());
            boolean isViewerClan = viewerClan != null && viewerClan.getName().equalsIgnoreCase(clan.getName());

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            if (skullMeta != null) {
                markAsMenuItem(skullMeta, "clan_list");
                OfflinePlayer leader = Bukkit.getOfflinePlayer(clan.getOwner());
                Player leaderOnline = Bukkit.getPlayer(clan.getOwner());
                if (leaderOnline != null) {
                    skullMeta.setPlayerProfile(leaderOnline.getPlayerProfile());
                } else {
                    skullMeta.setOwningPlayer(leader);
                }

                String leaderName = leader.getName() != null ? leader.getName()
                        : plugin.getPlaceholderManager().getUnknownText(lang);

                long onlineCount = clan.getMembers().keySet().stream()
                        .map(Bukkit::getPlayer)
                        .filter(Objects::nonNull)
                        .count();

                String rawTitle = itemTemplate != null
                        ? itemTemplate.getString("name", "&#00FF88&l%clan_name% &#718096[%clan_tag%]")
                        : "&#00FF88&l%clan_name% &#718096[%clan_tag%]";
                String processedTitle = replacePlaceholders(rawTitle, viewer, clan, page);
                skullMeta.displayName(MessageUtils.toComponentNoItalic(processedTitle));

                List<String> rawLore = itemTemplate != null ? itemTemplate.getStringList("lore") : null;
                List<net.kyori.adventure.text.Component> loreComponents = new ArrayList<>();

                if (rawLore != null && !rawLore.isEmpty()) {
                    for (String line : rawLore) {
                        String staffStatus = hasStaff
                                ? (lang.equalsIgnoreCase("es") ? "&#00FF88● Staff Conectado" : "&#00FF88● Staff Online")
                                : (lang.equalsIgnoreCase("es") ? "&#FF3366○ Sin Staff Conectado"
                                        : "&#FF3366○ No Staff Online");

                        String actionHint;
                        if (isViewerClan) {
                            actionHint = lang.equalsIgnoreCase("es") ? "&#00E5FF▶ Tu Clan Actual"
                                    : "&#00E5FF▶ Your Current Clan";
                        } else if (viewerInClan) {
                            actionHint = lang.equalsIgnoreCase("es") ? "&#718096Ya perteneces a un clan"
                                    : "&#718096Already in a clan";
                        } else if (clan.isOpenJoin()) {
                            actionHint = lang.equalsIgnoreCase("es") ? "&#00FF88▶ Clic para entrar (Clan Abierto)"
                                    : "&#00FF88▶ Click to join (Open Clan)";
                        } else if (alreadyRequested) {
                            actionHint = lang.equalsIgnoreCase("es") ? "&#FFAA00⌛ Solicitud Pendiente"
                                    : "&#FFAA00⌛ Request Pending";
                        } else if (hasStaff) {
                            actionHint = lang.equalsIgnoreCase("es") ? "&#00FF88▶ Clic para enviar solicitud"
                                    : "&#00FF88▶ Click to send join request";
                        } else {
                            actionHint = lang.equalsIgnoreCase("es") ? "&#FF5555✖ No disponible (Staff desconectado)"
                                    : "&#FF5555✖ Unavailable (Staff Offline)";
                        }

                        String processedLine = replacePlaceholders(line, viewer, clan, page)
                                .replace("%clan_staff_status%", staffStatus)
                                .replace("%action_hint%", actionHint);

                        loreComponents.add(MessageUtils.toComponentNoItalic(processedLine));
                    }
                } else {
                    loreComponents.add(MessageUtils.toComponentNoItalic("&#718096━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                    loreComponents.add(
                            MessageUtils.toComponentNoItalic("&#718096▪ &#A0AEC0Leader: &#FFFFFF" + leaderName));
                    loreComponents.add(MessageUtils
                            .toComponentNoItalic("&#718096▪ &#A0AEC0Tag: &#FFFFFF[" + clan.getTag() + "]"));
                    loreComponents.add(MessageUtils.toComponentNoItalic("&#718096▪ &#A0AEC0Members: &#FFFFFF"
                            + onlineCount + "&#718096/&#FFFFFF" + clan.getMemberCount()));
                    loreComponents.add(MessageUtils.toComponentNoItalic("&#718096▪ &#A0AEC0Friendly Fire: "
                            + (clan.isFriendlyFire() ? "&#00FF88ON" : "&#FF3366OFF")));
                    loreComponents.add(MessageUtils.toComponentNoItalic(
                            "&#718096▪ &#A0AEC0Staff: " + (hasStaff ? "&#00FF88● Online" : "&#FF3366○ Offline")));
                    loreComponents.add(MessageUtils.toComponentNoItalic("&#718096━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                    if (isViewerClan) {
                        loreComponents.add(MessageUtils.toComponentNoItalic("&#00E5FF▶ Your Current Clan"));
                    } else if (viewerInClan) {
                        loreComponents.add(MessageUtils.toComponentNoItalic("&#718096Already in a clan"));
                    } else if (clan.isOpenJoin()) {
                        loreComponents.add(MessageUtils.toComponentNoItalic("&#00FF88▶ Click to join (Open Clan)"));
                    } else if (alreadyRequested) {
                        loreComponents.add(MessageUtils.toComponentNoItalic("&#FFAA00⌛ Request Pending"));
                    } else if (hasStaff) {
                        loreComponents
                                .add(MessageUtils.toComponentNoItalic("&#00FF88▶ Click to send join request"));
                    } else {
                        loreComponents.add(MessageUtils.toComponentNoItalic("&#FF5555✖ Staff Offline"));
                    }
                }

                skullMeta.lore(loreComponents);
                head.setItemMeta(skullMeta);
            }

            inventory.setItem(targetSlot, head);
        }
    }

    private void renderDynamicTop(Inventory inventory, ConfigurationSection menuSection, Player viewer,
            ClanMenuHolder holder) {
        List<Integer> topSlots = menuSection.getIntegerList("top_slots");
        if (topSlots.isEmpty()) {
            int size = inventory.getSize();
            topSlots = new ArrayList<>();
            if (size >= 45) {
                for (int i = 10; i <= 16; i++) topSlots.add(i);
                for (int i = 19; i <= 25; i++) topSlots.add(i);
                for (int i = 28; i <= 34; i++) topSlots.add(i);
            } else {
                for (int i = 10; i <= 16; i++) topSlots.add(i);
            }
        }

        TopSortType sortType = holder != null ? holder.getTopSortType() : TopSortType.KDR;
        List<Clan> topClans = plugin.getClanManager().getTopClans(sortType);

        int itemsPerPage = topSlots.size();
        int totalClans = topClans.size();
        int maxPages = Math.max(1, (int) Math.ceil((double) totalClans / itemsPerPage));
        int page = holder != null ? holder.getPage() : 0;
        page = Math.min(Math.max(0, page), maxPages - 1);
        if (holder != null) {
            holder.setPage(page);
        }

        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, totalClans);
        int slotIndex = 0;

        String lang = plugin.getMainConfigManager().getSelectedLanguage();
        ConfigurationSection topItemConfig = menuSection.getConfigurationSection("top_item");

        for (int slot : topSlots) {
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, null);
            }
        }

        for (int i = startIndex; i < endIndex; i++) {
            if (slotIndex >= topSlots.size())
                break;

            Clan clan = topClans.get(i);
            int rank = i + 1;
            int targetSlot = topSlots.get(slotIndex++);
            if (holder != null) {
                holder.setClanAtSlot(targetSlot, clan.getName());
            }

            OfflinePlayer leader = Bukkit.getOfflinePlayer(clan.getOwner());
            Player leaderOnline = Bukkit.getPlayer(clan.getOwner());

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            if (skullMeta != null) {
                markAsMenuItem(skullMeta, "top");
                if (leaderOnline != null) {
                    skullMeta.setPlayerProfile(leaderOnline.getPlayerProfile());
                } else {
                    skullMeta.setOwningPlayer(leader);
                }

                String rankBadge;
                if (rank == 1) {
                    rankBadge = "&#FFD700&l#1 ✦";
                } else if (rank == 2) {
                    rankBadge = "&#E2E8F0&l#2 ✦";
                } else if (rank == 3) {
                    rankBadge = "&#CD7F32&l#3 ✦";
                } else {
                    rankBadge = "&#718096#" + rank;
                }

                String leaderName = leaderOnline != null ? leaderOnline.getName()
                        : (leader.getName() != null ? leader.getName() : plugin.getPlaceholderManager().getUnknownText(lang));

                long onlineCount = clan.getMembers().keySet().stream()
                        .map(Bukkit::getPlayer)
                        .filter(Objects::nonNull)
                        .count();

                if (topItemConfig != null) {
                    String rawTitle = topItemConfig.getString("name", "%clan_rank% &#00FF88&l%clan_name% &#718096[%clan_tag%]");
                    String processedTitle = rawTitle.replace("%clan_rank%", rankBadge)
                            .replace("%rank%", String.valueOf(rank))
                            .replace("%clan_name%", clan.getName())
                            .replace("%clan_tag%", clan.getTag())
                            .replace("%clan_leader%", leaderName)
                            .replace("%clan_kdr%", clan.getFormattedKDR())
                            .replace("%clan_kills%", String.valueOf(clan.getKills()))
                            .replace("%clan_deaths%", String.valueOf(clan.getDeaths()))
                            .replace("%clan_rival_kills%", String.valueOf(clan.getRivalKills()))
                            .replace("%clan_members%", String.valueOf(clan.getMemberCount()))
                            .replace("%clan_members_online%", String.valueOf(onlineCount));
                    skullMeta.displayName(MessageUtils.toComponentNoItalic(processedTitle));

                    List<String> rawLore = topItemConfig.getStringList("lore");
                    List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
                    for (String line : rawLore) {
                        String processedLine = line.replace("%clan_rank%", rankBadge)
                                .replace("%rank%", String.valueOf(rank))
                                .replace("%clan_name%", clan.getName())
                                .replace("%clan_tag%", clan.getTag())
                                .replace("%clan_leader%", leaderName)
                                .replace("%clan_kdr%", clan.getFormattedKDR())
                                .replace("%clan_kills%", String.valueOf(clan.getKills()))
                                .replace("%clan_deaths%", String.valueOf(clan.getDeaths()))
                                .replace("%clan_rival_kills%", String.valueOf(clan.getRivalKills()))
                                .replace("%clan_members%", String.valueOf(clan.getMemberCount()))
                                .replace("%clan_members_online%", String.valueOf(onlineCount))
                                .replace("%clan_created%", plugin.getPlaceholderManager().formatDate(clan.getCreatedAt()));
                        lore.add(MessageUtils.toComponentNoItalic(processedLine));
                    }
                    skullMeta.lore(lore);
                } else {
                    skullMeta.displayName(MessageUtils.toComponentNoItalic(rankBadge + " &#00FF88&l" + clan.getName() + " &#718096[" + clan.getTag() + "]"));
                    List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
                    lore.add(MessageUtils.toComponentNoItalic("&#718096━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                    lore.add(MessageUtils.toComponentNoItalic("&#718096▪ &#A0AEC0Líder: &#FFFFFF" + leaderName));
                    lore.add(MessageUtils.toComponentNoItalic("&#718096▪ &#A0AEC0KDR: &#00FF88" + clan.getFormattedKDR() + " &#718096(&#FFFFFF" + clan.getKills() + " &#718096K / &#FFFFFF" + clan.getDeaths() + " &#718096D)"));
                    lore.add(MessageUtils.toComponentNoItalic("&#718096▪ &#A0AEC0Bajas Rivales: &#FF3366" + clan.getRivalKills()));
                    lore.add(MessageUtils.toComponentNoItalic("&#718096▪ &#A0AEC0Miembros: &#00E5FF" + onlineCount + "&#718096/&#FFFFFF" + clan.getMemberCount()));
                    lore.add(MessageUtils.toComponentNoItalic("&#718096━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                    skullMeta.lore(lore);
                }

                head.setItemMeta(skullMeta);
            }
            inventory.setItem(targetSlot, head);
        }
    }

    private ItemStack createItem(ConfigurationSection itemConfig, Player player, Clan clan, int page) {
        return createItem(itemConfig, player, clan, page, null, null);
    }

    private ItemStack createItem(ConfigurationSection itemConfig, Player player, Clan clan, int page,
            ClanMenuHolder holder) {
        return createItem(itemConfig, player, clan, page, holder, holder != null ? holder.getMenuId() : null);
    }

    private ItemStack createItem(ConfigurationSection itemConfig, Player player, Clan clan, int page,
            ClanMenuHolder holder, String menuId) {
        String materialName = itemConfig.getString("material", "STONE");
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            material = Material.STONE;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return item;

        markAsMenuItem(meta, menuId);

        String name = itemConfig.getString("name", "");
        String processedName = replacePlaceholders(name, player, clan, page, holder);
        meta.displayName(MessageUtils.toComponentNoItalic(processedName));

        List<String> lore = itemConfig.getStringList("lore");
        if (lore != null && !lore.isEmpty()) {
            List<net.kyori.adventure.text.Component> loreComponents = new ArrayList<>();
            for (String line : lore) {
                String processedLine = replacePlaceholders(line, player, clan, page, holder);
                loreComponents.add(MessageUtils.toComponentNoItalic(processedLine));
            }
            meta.lore(loreComponents);
        }

        if (material == Material.PLAYER_HEAD && meta instanceof SkullMeta skullMeta) {
            String base64 = itemConfig.getString("base64");
            String owner = itemConfig.getString("owner");

            if (owner != null && !owner.trim().isEmpty()) {
                String rawOwner = owner.trim();
                if (rawOwner.equalsIgnoreCase("%player%") || rawOwner.equalsIgnoreCase("{player}")
                        || rawOwner.equalsIgnoreCase("self") || rawOwner.equalsIgnoreCase(player.getName())) {
                    skullMeta.setPlayerProfile(player.getPlayerProfile());
                } else if (rawOwner.equalsIgnoreCase("%clan_leader%") || rawOwner.equalsIgnoreCase("{clan_leader}")) {
                    if (clan != null && clan.getOwner() != null) {
                        Player leaderOnline = Bukkit.getPlayer(clan.getOwner());
                        if (leaderOnline != null) {
                            skullMeta.setPlayerProfile(leaderOnline.getPlayerProfile());
                        } else {
                            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(clan.getOwner()));
                        }
                    } else {
                        skullMeta.setPlayerProfile(player.getPlayerProfile());
                    }
                } else {
                    String resolved = replacePlaceholders(rawOwner, player, clan, page, holder);
                    Player resolvedOnline = Bukkit.getPlayer(resolved);
                    if (resolvedOnline != null) {
                        skullMeta.setPlayerProfile(resolvedOnline.getPlayerProfile());
                    } else {
                        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(resolved));
                    }
                }
            } else if (base64 != null && !base64.trim().isEmpty()) {
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                profile.setProperty(new ProfileProperty("textures", base64));
                skullMeta.setPlayerProfile(profile);
            } else {
                skullMeta.setPlayerProfile(player.getPlayerProfile());
            }
        }

        if (itemConfig.getBoolean("glow", false)) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        if (itemConfig.contains("custom_model_data")) {
            org.bukkit.inventory.meta.components.CustomModelDataComponent component = meta
                    .getCustomModelDataComponent();
            component.setFloats(java.util.List.of((float) itemConfig.getInt("custom_model_data")));
            meta.setCustomModelDataComponent(component);
        }

        item.setItemMeta(meta);
        return item;
    }

    public List<Integer> getSlotsFromConfig(ConfigurationSection itemConfig) {
        List<Integer> slots = new ArrayList<>();
        if (itemConfig.contains("slots")) {
            if (itemConfig.isList("slots")) {
                slots.addAll(itemConfig.getIntegerList("slots"));
            } else {
                String rawSlots = itemConfig.getString("slots", "");
                for (String part : rawSlots.split(",")) {
                    part = part.trim();
                    if (part.contains("-")) {
                        String[] range = part.split("-");
                        try {
                            int start = Integer.parseInt(range[0].trim());
                            int end = Integer.parseInt(range[1].trim());
                            for (int s = start; s <= end; s++) {
                                slots.add(s);
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    } else {
                        try {
                            slots.add(Integer.parseInt(part));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
        } else if (itemConfig.contains("slot")) {
            slots.add(itemConfig.getInt("slot", 0));
        }
        return slots;
    }

    public String replacePlaceholders(String text, Player player, Clan clan) {
        return replacePlaceholders(text, player, clan, 0, null);
    }

    public String replacePlaceholders(String text, Player player, Clan clan, int page) {
        return replacePlaceholders(text, player, clan, page, null);
    }

    public String replacePlaceholders(String text, Player player, Clan clan, int page, ClanMenuHolder holder) {
        if (text == null)
            return "";

        String playerName = player.getName();
        text = text.replace("%player%", playerName).replace("{player}", playerName);

        int totalClans = getVisibleClansFor(player).size();
        FileConfiguration menusConfig = plugin.getMainConfigManager().getMenusConfig();
        List<Integer> clanSlots = menusConfig != null ? menusConfig.getIntegerList("menus.clan_list.clan_slots") : null;
        int itemsPerPage = (clanSlots != null && !clanSlots.isEmpty()) ? clanSlots.size() : 21;
        int maxPages = Math.max(1, (int) Math.ceil((double) totalClans / itemsPerPage));

        if (holder != null && holder.getMenuId().equalsIgnoreCase("members")) {
            List<Integer> memberSlots = menusConfig != null ? menusConfig.getIntegerList("menus.members.member_slots") : null;
            int memItemsPerPage = (memberSlots != null && !memberSlots.isEmpty()) ? memberSlots.size() : 21;
            int totalMembers = clan != null ? clan.getMemberCount() : 0;
            maxPages = Math.max(1, (int) Math.ceil((double) totalMembers / memItemsPerPage));
        } else if (holder != null && (holder.getMenuId().equalsIgnoreCase("top") || holder.getMenuId().equalsIgnoreCase("clan_top"))) {
            List<Integer> topSlots = menusConfig != null ? menusConfig.getIntegerList("menus." + holder.getMenuId() + ".top_slots") : null;
            int topItemsPerPage = (topSlots != null && !topSlots.isEmpty()) ? topSlots.size() : 21;
            int totalTop = plugin.getClanManager().getTopClans(holder.getTopSortType()).size();
            maxPages = Math.max(1, (int) Math.ceil((double) totalTop / topItemsPerPage));
        }

        int displayPage = Math.min(Math.max(0, page), maxPages - 1) + 1;

        text = text.replace("%page%", String.valueOf(displayPage))
                .replace("%max_pages%", String.valueOf(maxPages))
                .replace("%total_clans%", String.valueOf(totalClans));

        String lang = plugin.getMainConfigManager().getSelectedLanguage();
        org.zkaleejoo.managers.PlaceholderManager pm = plugin.getPlaceholderManager();

        MemberSortType currentSort = holder != null ? holder.getMemberSortType() : MemberSortType.ROLE;
        String sortName = pm != null ? pm.getSortTypeName(currentSort, lang) : currentSort.name();
        text = text.replace("%sort_mode%", sortName)
                .replace("%member_sort_mode%", sortName)
                .replace("{sort_mode}", sortName)
                .replace("{member_sort_mode}", sortName);

        TopSortType currentTopSort = holder != null ? holder.getTopSortType() : TopSortType.KDR;
        String topSortName = pm != null ? pm.getTopSortTypeName(currentTopSort, lang) : currentTopSort.name();
        text = text.replace("%top_sort_mode%", topSortName)
                .replace("{top_sort_mode}", topSortName);

        if (clan != null) {
            String clanName = clan.getName();
            String clanTag = clan.getTag();
            String memberCount = String.valueOf(clan.getMemberCount());

            long onlineCount = clan.getMembers().keySet().stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .count();
            String membersOnline = String.valueOf(onlineCount);

            OfflinePlayer leader = Bukkit.getOfflinePlayer(clan.getOwner());
            String leaderName = leader.getName() != null ? leader.getName()
                    : (pm != null ? pm.getUnknownText(lang) : "Unknown");

            ClanPlayer cp = clan.getMember(player.getUniqueId());
            String roleFormatted = pm != null ? pm.getRoleFormatted(cp != null ? cp.getRole() : null, lang, true)
                    : "<#718096>None";

            String ffText = pm != null ? pm.getFriendlyFireStatus(clan.isFriendlyFire(), lang, true)
                    : (clan.isFriendlyFire() ? "ON" : "OFF");
            String ffBadge = pm != null ? pm.getFriendlyFireBadge(clan.isFriendlyFire(), lang, true)
                    : (clan.isFriendlyFire() ? "ENABLED" : "DISABLED");
            String createdDate = pm != null ? pm.formatDate(clan.getCreatedAt()) : String.valueOf(clan.getCreatedAt());

            String clanDisplayName = clan.getDisplayName();
            String clanDisplayNameFormatted = pm != null ? pm.formatOutput(clanDisplayName, true) : clanDisplayName;
            String tagFormatted = pm != null ? pm.formatTag(clanTag, true) : "[" + clanTag + "]";

            text = text.replace("%clan_name%", clanName).replace("{clan_name}", clanName);
            text = text.replace("%clan_displayname%", clanDisplayName).replace("{clan_displayname}", clanDisplayName);
            text = text.replace("%clan_display_name%", clanDisplayName).replace("{clan_display_name}", clanDisplayName);
            text = text.replace("%clan_name_formatted%", clanDisplayNameFormatted).replace("{clan_name_formatted}", clanDisplayNameFormatted);
            text = text.replace("%clan_tag_formatted%", tagFormatted).replace("{clan_tag_formatted}", tagFormatted);
            text = text.replace("%clan_tag%", clanTag + "<reset>").replace("{clan_tag}", clanTag + "<reset>");
            text = text.replace("%clan_members%", memberCount).replace("{clan_members}", memberCount);
            text = text.replace("%clan_members_online%", membersOnline).replace("{clan_members_online}", membersOnline);
            text = text.replace("%clan_leader%", leaderName).replace("{clan_leader}", leaderName);
            text = text.replace("%clan_role%", roleFormatted).replace("{clan_role}", roleFormatted);
            text = text.replace("%clan_ff%", ffText).replace("{clan_ff}", ffText);
            text = text.replace("%clan_ff_badge%", ffBadge).replace("{clan_ff_badge}", ffBadge);
            text = text.replace("%clan_created%", createdDate).replace("{clan_created}", createdDate);
            text = text.replace("%clan_kills%", String.valueOf(clan.getKills())).replace("{clan_kills}", String.valueOf(clan.getKills()));
            text = text.replace("%clan_deaths%", String.valueOf(clan.getDeaths())).replace("{clan_deaths}", String.valueOf(clan.getDeaths()));
            text = text.replace("%clan_kdr%", clan.getFormattedKDR()).replace("{clan_kdr}", clan.getFormattedKDR());
            text = text.replace("%clan_rival_kills%", String.valueOf(clan.getRivalKills())).replace("{clan_rival_kills}", String.valueOf(clan.getRivalKills()));

            for (ClanFlag flag : ClanFlag.values()) {
                boolean val = clan.getFlag(flag);
                String flagStatus = pm != null ? pm.getFlagStatus(flag, val, lang, true) : (val ? "ON" : "OFF");
                String flagBadge = pm != null ? pm.getFlagBadge(flag, val, lang, true) : (val ? "ENABLED" : "DISABLED");
                text = text.replace("%clan_flag_" + flag.getKey() + "%", flagStatus)
                        .replace("{clan_flag_" + flag.getKey() + "}", flagStatus)
                        .replace("%clan_flag_" + flag.getKey() + "_badge%", flagBadge)
                        .replace("{clan_flag_" + flag.getKey() + "_badge}", flagBadge);
            }
        } else {
            String noneText = pm != null ? pm.getNotInClanText(lang) : "None";
            String noClanTag = pm != null ? pm.getNoClanTag(lang) : "---";
            String noClanTagFormatted = pm != null ? pm.formatTag(noClanTag, true) : "---";
            String noRole = pm != null ? pm.getRoleFormatted(null, lang, true) : "<#718096>None";
            String noFfText = pm != null ? pm.getFriendlyFireStatus(false, lang, true) : "OFF";
            String noFfBadge = pm != null ? pm.getFriendlyFireBadge(false, lang, true) : "DISABLED";
            String noCreated = pm != null ? pm.getNoneText(lang) : "N/A";

            text = text.replace("%clan_name%", noneText).replace("{clan_name}", noneText);
            text = text.replace("%clan_tag%", noClanTag).replace("{clan_tag}", noClanTag);
            text = text.replace("%clan_tag_formatted%", noClanTagFormatted).replace("{clan_tag_formatted}", noClanTagFormatted);
            text = text.replace("%clan_members%", "0").replace("{clan_members}", "0");
            text = text.replace("%clan_members_online%", "0").replace("{clan_members_online}", "0");
            text = text.replace("%clan_leader%", noneText).replace("{clan_leader}", noneText);
            text = text.replace("%clan_role%", noRole).replace("{clan_role}", noRole);
            text = text.replace("%clan_ff%", noFfText).replace("{clan_ff}", noFfText);
            text = text.replace("%clan_ff_badge%", noFfBadge).replace("{clan_ff_badge}", noFfBadge);
            text = text.replace("%clan_created%", noCreated).replace("{clan_created}", noCreated);
            text = text.replace("%clan_kills%", "0").replace("{clan_kills}", "0");
            text = text.replace("%clan_deaths%", "0").replace("{clan_deaths}", "0");
            text = text.replace("%clan_kdr%", "0.00").replace("{clan_kdr}", "0.00");
            text = text.replace("%clan_rival_kills%", "0").replace("{clan_rival_kills}", "0");

            for (ClanFlag flag : ClanFlag.values()) {
                boolean val = flag.getDefaultValue();
                String flagStatus = pm != null ? pm.getFlagStatus(flag, val, lang, true) : "OFF";
                String flagBadge = pm != null ? pm.getFlagBadge(flag, val, lang, true) : "DISABLED";
                text = text.replace("%clan_flag_" + flag.getKey() + "%", flagStatus)
                        .replace("{clan_flag_" + flag.getKey() + "}", flagStatus)
                        .replace("%clan_flag_" + flag.getKey() + "_badge%", flagBadge)
                        .replace("{clan_flag_" + flag.getKey() + "_badge}", flagBadge);
            }
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            try {
                text = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
            } catch (Exception ignored) {
            }
        }

        return text;
    }

    public ConfigurationSection getItemConfig(String menuId, int slot) {
        FileConfiguration menusConfig = plugin.getMainConfigManager().getMenusConfig();
        ConfigurationSection itemsSection = menusConfig.getConfigurationSection("menus." + menuId + ".items");
        if (itemsSection == null)
            return null;

        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection item = itemsSection.getConfigurationSection(key);
            if (item != null) {
                List<Integer> slots = getSlotsFromConfig(item);
                if (slots.contains(slot)) {
                    return item;
                }
            }
        }
        return null;
    }

    public String getAction(String menuId, int slot) {
        ConfigurationSection item = getItemConfig(menuId, slot);
        if (item != null) {
            return item.getString("action", null);
        }
        return null;
    }
}
