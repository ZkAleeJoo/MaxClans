package org.zkaleejoo.managers;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.models.ClanFlag;
import org.zkaleejoo.models.ClanPlayer;
import org.zkaleejoo.models.ClanRole;
import org.zkaleejoo.models.TopSortType;
import org.zkaleejoo.utils.MessageUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class PlaceholderManager {

    private final OnlyClans plugin;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private String tagFormat = "&#8727F5[{tag}]";
    private String outputFormatMode = "minimessage";

    public PlaceholderManager(OnlyClans plugin) {
        this.plugin = plugin;
    }

    public void loadConfig(FileConfiguration config) {
        String pattern = config.getString("placeholders.date-format", "dd/MM/yyyy");
        try {
            this.dateFormat = new SimpleDateFormat(pattern);
        } catch (Exception e) {
            this.dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        }

        this.tagFormat = config.getString("placeholders.tag-format", "&#8727F5[{tag}]");
        this.outputFormatMode = config.getString("placeholders.format-mode", "minimessage").toLowerCase();
    }

    public boolean isMiniMessageMode() {
        return !"legacy".equalsIgnoreCase(outputFormatMode);
    }

    public String formatOutput(String text, boolean miniMessage) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return miniMessage ? MessageUtils.toMiniMessage(text) : MessageUtils.getColoredMessage(text);
    }

    public String getActiveLanguage() {
        return plugin.getMainConfigManager().getSelectedLanguage();
    }

    private ConfigurationSection getLangSection(String lang) {
        if (lang == null || lang.isEmpty()) {
            lang = getActiveLanguage();
        }
        FileConfiguration config = plugin.getMainConfigManager().getConfigFile();
        ConfigurationSection section = config.getConfigurationSection("placeholders.languages." + lang.toLowerCase());
        if (section == null) {
            section = config.getConfigurationSection("placeholders.languages.en");
        }
        return section;
    }

    public String getString(String lang, String path, String def) {
        ConfigurationSection section = getLangSection(lang);
        if (section != null && section.contains(path)) {
            return section.getString(path, def);
        }
        if (lang != null && !lang.equalsIgnoreCase("en")) {
            ConfigurationSection enSection = getLangSection("en");
            if (enSection != null && enSection.contains(path)) {
                return enSection.getString(path, def);
            }
        }
        return def;
    }

    public String getNoneText(String lang) {
        return getString(lang, "none", "None");
    }

    public String getNoClanTag(String lang) {
        return getString(lang, "no-clan-tag", "---");
    }

    public String getUnknownText(String lang) {
        return getString(lang, "unknown", "Unknown");
    }

    public String getNotInClanText(String lang) {
        return getString(lang, "not-in-clan", getNoneText(lang));
    }

    public String getRoleName(ClanRole role, String lang) {
        if (role == null) {
            return getString(lang, "roles.none", "None");
        }
        return switch (role) {
            case LEADER -> getString(lang, "roles.leader", "Leader");
            case MODERATOR -> getString(lang, "roles.moderator", "Moderator");
            case MEMBER -> getString(lang, "roles.member", "Member");
        };
    }

    public String getRoleFormatted(ClanRole role, String lang) {
        return getRoleFormatted(role, lang, isMiniMessageMode());
    }

    public String getRoleFormatted(ClanRole role, String lang, boolean miniMessage) {
        if (role == null) {
            return formatOutput(getString(lang, "roles-formatted.none", "&#94A3B8None"), miniMessage);
        }
        return formatOutput(switch (role) {
            case LEADER -> getString(lang, "roles-formatted.leader", "&#FDE047★ Leader");
            case MODERATOR -> getString(lang, "roles-formatted.moderator", "&#7DD3FC◆ Moderator");
            case MEMBER -> getString(lang, "roles-formatted.member", "&#CBD5E1● Member");
        }, miniMessage);
    }

    public String getFriendlyFireRaw(boolean ff, String lang) {
        return ff ? getString(lang, "friendly-fire.on", "ON") : getString(lang, "friendly-fire.off", "OFF");
    }

    public String getFriendlyFireBadge(boolean ff, String lang) {
        return getFriendlyFireBadge(ff, lang, isMiniMessageMode());
    }

    public String getFriendlyFireBadge(boolean ff, String lang, boolean miniMessage) {
        return formatOutput(ff ? getString(lang, "friendly-fire.badge-enabled", "&#6EE7B7&lENABLED")
                : getString(lang, "friendly-fire.badge-disabled", "&#FDA4AF&lDISABLED"), miniMessage);
    }

    public String getFriendlyFireStatus(boolean ff, String lang) {
        return getFriendlyFireStatus(ff, lang, isMiniMessageMode());
    }

    public String getFriendlyFireStatus(boolean ff, String lang, boolean miniMessage) {
        return formatOutput(ff ? getString(lang, "friendly-fire.status-enabled", "&#6EE7B7✔ Enabled")
                : getString(lang, "friendly-fire.status-disabled", "&#FDA4AF✖ Disabled"), miniMessage);
    }

    public String getFlagStatus(ClanFlag flag, boolean value, String lang) {
        return getFlagStatus(flag, value, lang, isMiniMessageMode());
    }

    public String getFlagStatus(ClanFlag flag, boolean value, String lang, boolean miniMessage) {
        return formatOutput(value
                ? getString(lang, "flags.status-enabled",
                        getString(lang, "friendly-fire.status-enabled", "&#6EE7B7✔ Enabled"))
                : getString(lang, "flags.status-disabled",
                        getString(lang, "friendly-fire.status-disabled", "&#FDA4AF✖ Disabled")),
                miniMessage);
    }

    public String getFlagBadge(ClanFlag flag, boolean value, String lang) {
        return getFlagBadge(flag, value, lang, isMiniMessageMode());
    }

    public String getFlagBadge(ClanFlag flag, boolean value, String lang, boolean miniMessage) {
        return formatOutput(value
                ? getString(lang, "flags.badge-enabled",
                        getString(lang, "friendly-fire.badge-enabled", "&#6EE7B7&lENABLED"))
                : getString(lang, "flags.badge-disabled",
                        getString(lang, "friendly-fire.badge-disabled", "&#FDA4AF&lDISABLED")),
                miniMessage);
    }

    public String getGuiText(String key, String lang, String def) {
        return getGuiText(key, lang, def, isMiniMessageMode());
    }

    public String getGuiText(String key, String lang, String def, boolean miniMessage) {
        return formatOutput(getString(lang, "gui." + key, def), miniMessage);
    }

    public String formatTag(String tag) {
        return formatTag(tag, isMiniMessageMode());
    }

    public String formatTag(String tag, boolean miniMessage) {
        if (tag == null || tag.isEmpty())
            return "";
        String replaced = tagFormat.replace("{tag}", tag);
        return formatOutput(replaced, miniMessage);
    }

    public String formatDate(long timestamp) {
        return dateFormat.format(new Date(timestamp));
    }

    public String formatPlaytime(long ticks, String lang) {
        long seconds = ticks / 20L;
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;

        if (days > 0) {
            return days + "d " + hours + "h " + minutes + "m";
        }
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return Math.max(0, minutes) + "m";
    }

    public String formatKDR(int kills, int deaths) {
        double kdr = (deaths <= 0) ? (double) kills : (double) kills / deaths;
        return String.format(java.util.Locale.US, "%.2f", kdr);
    }

    public String getSortTypeName(org.zkaleejoo.models.MemberSortType type, String lang) {
        if (type == null) {
            return getGuiText("sort-role", lang, "&#FDE047Role");
        }
        return switch (type) {
            case ROLE -> getGuiText("sort-role", lang, "&#FDE047Role");
            case KDR -> getGuiText("sort-kdr", lang, "&#6EE7B7KDR");
            case PLAYTIME -> getGuiText("sort-playtime", lang, "&#7DD3FCPlaytime");
            case JOIN_RECENT -> getGuiText("sort-join-recent", lang, "&#FDE047Recent");
            case JOIN_OLDEST -> getGuiText("sort-join-oldest", lang, "&#CBD5E1Oldest");
        };
    }

    public String getTopSortTypeName(TopSortType type, String lang) {
        if (type == null) {
            return getGuiText("sort-top-kdr", lang, "&#6EE7B7KDR (Ratio)");
        }
        return switch (type) {
            case KDR -> getGuiText("sort-top-kdr", lang, "&#6EE7B7KDR (Ratio)");
            case KILLS -> getGuiText("sort-top-kills", lang, "&#FDA4AFKills (Most Bloodthirsty)");
            case MEMBERS -> getGuiText("sort-top-members", lang, "&#7DD3FCMembers (Largest)");
        };
    }

    public String resolveTopPlaceholder(String param, String lang) {
        return resolveTopPlaceholder(param, lang, isMiniMessageMode());
    }

    public String resolveTopPlaceholder(String param, String lang, boolean useMiniMessage) {
        String rest = param.substring(4);
        TopSortType sortType = null;
        String afterType = null;

        if (rest.startsWith("kdr_")) {
            sortType = TopSortType.KDR;
            afterType = rest.substring(4);
        } else if (rest.startsWith("kills_")) {
            sortType = TopSortType.KILLS;
            afterType = rest.substring(6);
        } else if (rest.startsWith("members_")) {
            sortType = TopSortType.MEMBERS;
            afterType = rest.substring(8);
        } else if (rest.startsWith("member_")) {
            sortType = TopSortType.MEMBERS;
            afterType = rest.substring(7);
        }

        if (sortType != null && afterType != null) {
            int firstUnderscore = afterType.indexOf('_');
            if (firstUnderscore > 0) {
                String rankStr = afterType.substring(0, firstUnderscore);
                String prop = afterType.substring(firstUnderscore + 1);

                try {
                    int rank = Integer.parseInt(rankStr);
                    Clan topClan = plugin.getClanManager().getTopClan(sortType, rank);

                    if (prop.equals("name")) {
                        return topClan != null ? topClan.getName() : getNoneText(lang);
                    } else if (prop.equals("displayname") || prop.equals("display_name")
                            || prop.equals("name_formatted")) {
                        return topClan != null ? formatOutput(topClan.getDisplayName(), useMiniMessage)
                                : getNoneText(lang);
                    } else if (prop.equals("tag")) {
                        return topClan != null ? formatOutput(topClan.getTag(), useMiniMessage) : getNoClanTag(lang);
                    } else if (prop.equals("tag_raw")) {
                        return topClan != null ? MessageUtils.stripColor(topClan.getTag()) : getNoClanTag(lang);
                    } else if (prop.equals("tag_formatted")) {
                        return topClan != null ? formatTag(topClan.getTag(), useMiniMessage) : "";
                    } else if (prop.equals("leader")) {
                        if (topClan == null)
                            return getNoneText(lang);
                        OfflinePlayer leader = Bukkit.getOfflinePlayer(topClan.getOwner());
                        return leader.getName() != null ? leader.getName() : getUnknownText(lang);
                    } else if (prop.equals("val") || prop.equals("value")) {
                        if (topClan == null) {
                            return sortType == TopSortType.KDR ? "0.00" : "0";
                        }
                        return switch (sortType) {
                            case KDR -> topClan.getFormattedKDR();
                            case KILLS -> String.valueOf(topClan.getKills());
                            case MEMBERS -> String.valueOf(topClan.getMemberCount());
                        };
                    } else if (prop.equals("kdr")) {
                        return topClan != null ? topClan.getFormattedKDR() : "0.00";
                    } else if (prop.equals("kills")) {
                        return topClan != null ? String.valueOf(topClan.getKills()) : "0";
                    } else if (prop.equals("deaths")) {
                        return topClan != null ? String.valueOf(topClan.getDeaths()) : "0";
                    } else if (prop.equals("rival_kills")) {
                        return topClan != null ? String.valueOf(topClan.getRivalKills()) : "0";
                    } else if (prop.equals("members")) {
                        return topClan != null ? String.valueOf(topClan.getMemberCount()) : "0";
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }

    public String resolvePlaceholder(OfflinePlayer player, String rawParam) {
        if (rawParam == null) {
            return "";
        }

        String param = rawParam.toLowerCase();
        String lang = getActiveLanguage();

        boolean forceMiniMessage = false;
        boolean forceLegacy = false;
        boolean forceRaw = false;

        if (param.endsWith("_mm")) {
            forceMiniMessage = true;
            param = param.substring(0, param.length() - 3);
        } else if (param.endsWith("_minimessage")) {
            forceMiniMessage = true;
            param = param.substring(0, param.length() - 12);
        } else if (param.endsWith("_legacy")) {
            forceLegacy = true;
            param = param.substring(0, param.length() - 7);
        }

        if (param.endsWith("_en") || param.endsWith("_es")) {
            lang = param.substring(param.length() - 2);
            param = param.substring(0, param.length() - 3);
        } else if (param.startsWith("en_") || param.startsWith("es_")) {
            lang = param.substring(0, 2);
            param = param.substring(3);
        }

        if (!forceMiniMessage && !forceLegacy) {
            if (param.endsWith("_mm")) {
                forceMiniMessage = true;
                param = param.substring(0, param.length() - 3);
            } else if (param.endsWith("_minimessage")) {
                forceMiniMessage = true;
                param = param.substring(0, param.length() - 12);
            } else if (param.endsWith("_legacy")) {
                forceLegacy = true;
                param = param.substring(0, param.length() - 7);
            }
        }

        boolean useMiniMessage = forceMiniMessage || (!forceLegacy && isMiniMessageMode());

        if (param.startsWith("top_")) {
            String res = resolveTopPlaceholder(param, lang, useMiniMessage);
            return forceRaw && res != null ? MessageUtils.stripColor(res) : res;
        }

        if (player == null) {
            return "";
        }

        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());

        if (param.equals("clan_kills")) {
            return clan != null ? String.valueOf(clan.getKills()) : "0";
        }
        if (param.equals("clan_deaths")) {
            return clan != null ? String.valueOf(clan.getDeaths()) : "0";
        }
        if (param.equals("clan_kdr")) {
            return clan != null ? clan.getFormattedKDR() : "0.00";
        }
        if (param.equals("clan_rival_kills") || param.equals("rival_kills")) {
            return clan != null ? String.valueOf(clan.getRivalKills()) : "0";
        }

        String cleanParam = param.startsWith("clan_") ? param.substring(5) : param;

        if (cleanParam.equals("name")) {
            return clan != null ? clan.getName() : "";
        }

        if (cleanParam.equals("displayname") || cleanParam.equals("display_name")
                || cleanParam.equals("name_formatted")) {
            if (clan == null)
                return "";
            return formatOutput(clan.getDisplayName(), useMiniMessage);
        }

        if (cleanParam.equals("tag")) {
            if (clan == null)
                return "";
            return formatOutput(clan.getTag(), useMiniMessage);
        }

        if (cleanParam.equals("tag_raw")) {
            return clan != null ? MessageUtils.stripColor(clan.getTag()) : "";
        }

        if (cleanParam.equals("tag_formatted")) {
            return clan != null ? formatTag(clan.getTag(), useMiniMessage) : "";
        }

        if (cleanParam.equals("role_raw")) {
            if (clan == null)
                return "";
            ClanPlayer cp = clan.getMember(player.getUniqueId());
            return cp != null ? cp.getRole().name() : "";
        }

        if (cleanParam.equals("role")) {
            if (clan == null)
                return "";
            ClanPlayer cp = clan.getMember(player.getUniqueId());
            return cp != null ? getRoleName(cp.getRole(), lang) : "";
        }

        if (cleanParam.equals("role_formatted")) {
            if (clan == null)
                return "";
            ClanPlayer cp = clan.getMember(player.getUniqueId());
            return cp != null ? getRoleFormatted(cp.getRole(), lang, useMiniMessage) : "";
        }

        if (cleanParam.equals("members_count") || cleanParam.equals("members")) {
            return clan != null ? String.valueOf(clan.getMemberCount()) : "0";
        }

        if (cleanParam.equals("members_online")) {
            if (clan == null)
                return "0";
            long onlineCount = clan.getMembers().keySet().stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .count();
            return String.valueOf(onlineCount);
        }

        if (cleanParam.equals("leader")) {
            if (clan == null)
                return "";
            OfflinePlayer leader = Bukkit.getOfflinePlayer(clan.getOwner());
            return leader.getName() != null ? leader.getName() : "";
        }

        if (cleanParam.equals("ff")) {
            if (clan == null)
                return getFriendlyFireRaw(false, lang);
            return getFriendlyFireRaw(clan.isFriendlyFire(), lang);
        }

        if (cleanParam.equals("ff_badge")) {
            if (clan == null)
                return getFriendlyFireBadge(false, lang, useMiniMessage);
            return getFriendlyFireBadge(clan.isFriendlyFire(), lang, useMiniMessage);
        }

        if (cleanParam.equals("ff_status")) {
            if (clan == null)
                return getFriendlyFireStatus(false, lang, useMiniMessage);
            return getFriendlyFireStatus(clan.isFriendlyFire(), lang, useMiniMessage);
        }

        if (cleanParam.startsWith("flag_")) {
            String rest = cleanParam.substring(5);
            boolean isBadge = rest.endsWith("_badge");
            boolean isStatus = rest.endsWith("_status");
            String flagKey = rest;
            if (isBadge) {
                flagKey = rest.substring(0, rest.length() - 6);
            } else if (isStatus) {
                flagKey = rest.substring(0, rest.length() - 7);
            }

            ClanFlag flag = ClanFlag.fromKey(flagKey);
            if (flag != null) {
                boolean val = clan != null ? clan.getFlag(flag) : flag.getDefaultValue();
                if (isBadge) {
                    return getFlagBadge(flag, val, lang, useMiniMessage);
                } else {
                    return getFlagStatus(flag, val, lang, useMiniMessage);
                }
            }
        }

        if (cleanParam.equals("created")) {
            if (clan == null)
                return "";
            return formatDate(clan.getCreatedAt());
        }

        if (cleanParam.equals("kills") || cleanParam.equals("player_kills")) {
            try {
                return String.valueOf(player.getStatistic(org.bukkit.Statistic.PLAYER_KILLS));
            } catch (Exception ignored) {
                return "0";
            }
        }

        if (cleanParam.equals("deaths") || cleanParam.equals("player_deaths")) {
            try {
                return String.valueOf(player.getStatistic(org.bukkit.Statistic.DEATHS));
            } catch (Exception ignored) {
                return "0";
            }
        }

        if (cleanParam.equals("kdr") || cleanParam.equals("player_kdr")) {
            try {
                int kills = player.getStatistic(org.bukkit.Statistic.PLAYER_KILLS);
                int deaths = player.getStatistic(org.bukkit.Statistic.DEATHS);
                return formatKDR(kills, deaths);
            } catch (Exception ignored) {
                return "0.00";
            }
        }

        if (cleanParam.equals("playtime") || cleanParam.equals("player_playtime")) {
            try {
                int ticks = player.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE);
                return formatPlaytime(ticks, lang);
            } catch (Exception ignored) {
                return "0m";
            }
        }

        if (cleanParam.equals("joined") || cleanParam.equals("joined_clan")) {
            if (clan == null)
                return "";
            ClanPlayer cp = clan.getMember(player.getUniqueId());
            if (cp == null)
                return "";
            long joinedAt = cp.getJoinedAt() > 0 ? cp.getJoinedAt() : clan.getCreatedAt();
            return formatDate(joinedAt);
        }

        if (cleanParam.equals("level") || cleanParam.equals("clan_level")) {
            return clan != null ? String.valueOf(clan.getLevel()) : "1";
        }

        if (cleanParam.equals("exp") || cleanParam.equals("clan_exp")) {
            return clan != null ? String.valueOf(clan.getExp()) : "0";
        }

        if (cleanParam.equals("exp_next") || cleanParam.equals("clan_exp_next") || cleanParam.equals("exp_needed")
                || cleanParam.equals("clan_exp_needed")) {
            if (clan == null)
                return "0";
            return String.valueOf(plugin.getClanLevelManager().getExpForNextLevel(clan));
        }

        if (cleanParam.equals("exp_percent") || cleanParam.equals("clan_exp_percent")
                || cleanParam.equals("exp_percentage")) {
            if (clan == null)
                return "0.0%";
            return String.format(java.util.Locale.US, "%.1f%%", plugin.getClanLevelManager().getExpPercentage(clan));
        }

        if (cleanParam.equals("exp_bar") || cleanParam.equals("clan_exp_bar")) {
            if (clan == null)
                return "";
            return formatOutput(plugin.getClanLevelManager().getExpProgressBar(clan, 20), useMiniMessage);
        }

        if (cleanParam.equals("bank") || cleanParam.equals("clan_bank") || cleanParam.equals("bank_balance")
                || cleanParam.equals("clan_bank_balance")) {
            if (clan == null)
                return "0.00";
            return String.format(java.util.Locale.US, "%.2f", clan.getBankBalance());
        }

        if (cleanParam.equals("max_members") || cleanParam.equals("clan_max_members")
                || cleanParam.equals("members_max")) {
            if (clan == null)
                return "8";
            return String.valueOf(plugin.getClanLevelManager().getMaxMembers(clan.getLevel()));
        }

        if (cleanParam.equals("allies") || cleanParam.equals("clan_allies") || cleanParam.equals("allies_count")
                || cleanParam.equals("clan_allies_count")) {
            if (clan == null)
                return "0";
            return String.valueOf(clan.getAllies().size());
        }

        if (cleanParam.equals("max_allies") || cleanParam.equals("clan_max_allies")
                || cleanParam.equals("allies_max")) {
            if (clan == null)
                return "0";
            return String.valueOf(plugin.getClanLevelManager().getMaxAllies(clan.getLevel()));
        }

        if (cleanParam.equals("allies_list") || cleanParam.equals("clan_allies_list")) {
            if (clan == null || clan.getAllies().isEmpty())
                return getNoneText(lang);
            return String.join(", ", clan.getAllies());
        }

        if (cleanParam.equals("homes_count") || cleanParam.equals("home_count") || cleanParam.equals("homes")) {
            return clan != null ? String.valueOf(clan.getHomeCount()) : "0";
        }

        if (cleanParam.equals("homes_max") || cleanParam.equals("home_max") || cleanParam.equals("max_homes")
                || cleanParam.equals("clan_max_homes")) {
            if (clan == null)
                return "1";
            return String.valueOf(plugin.getClanLevelManager().getMaxHomes(clan.getLevel()));
        }

        if (cleanParam.equals("homes_list") || cleanParam.equals("home_list")) {
            if (clan == null || clan.getHomeCount() == 0)
                return getNoneText(lang);
            return String.join(", ", clan.getHomeNames());
        }

        return null;
    }
}
