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
        if (role == null) {
            return MessageUtils.getColoredMessage(getString(lang, "roles-formatted.none", "&#718096None"));
        }
        return MessageUtils.getColoredMessage(switch (role) {
            case LEADER -> getString(lang, "roles-formatted.leader", "&#FFD700★ Leader");
            case MODERATOR -> getString(lang, "roles-formatted.moderator", "&#00E5FF◆ Moderator");
            case MEMBER -> getString(lang, "roles-formatted.member", "&#A0AEC0● Member");
        });
    }

    public String getFriendlyFireRaw(boolean ff, String lang) {
        return ff ? getString(lang, "friendly-fire.on", "ON") : getString(lang, "friendly-fire.off", "OFF");
    }

    public String getFriendlyFireBadge(boolean ff, String lang) {
        return MessageUtils.getColoredMessage(ff ? getString(lang, "friendly-fire.badge-enabled", "&#00FF88&lENABLED")
                : getString(lang, "friendly-fire.badge-disabled", "&#FF3366&lDISABLED"));
    }

    public String getFriendlyFireStatus(boolean ff, String lang) {
        return MessageUtils.getColoredMessage(ff ? getString(lang, "friendly-fire.status-enabled", "&#00FF88✔ Enabled")
                : getString(lang, "friendly-fire.status-disabled", "&#FF3366✖ Disabled"));
    }

    public String getFlagStatus(ClanFlag flag, boolean value, String lang) {
        return MessageUtils.getColoredMessage(value
                ? getString(lang, "flags.status-enabled", getString(lang, "friendly-fire.status-enabled", "&#00FF88✔ Enabled"))
                : getString(lang, "flags.status-disabled", getString(lang, "friendly-fire.status-disabled", "&#FF3366✖ Disabled")));
    }

    public String getFlagBadge(ClanFlag flag, boolean value, String lang) {
        return MessageUtils.getColoredMessage(value
                ? getString(lang, "flags.badge-enabled", getString(lang, "friendly-fire.badge-enabled", "&#00FF88&lENABLED"))
                : getString(lang, "flags.badge-disabled", getString(lang, "friendly-fire.badge-disabled", "&#FF3366&lDISABLED")));
    }

    public String getGuiText(String key, String lang, String def) {
        return MessageUtils.getColoredMessage(getString(lang, "gui." + key, def));
    }

    public String formatTag(String tag) {
        if (tag == null || tag.isEmpty())
            return "";
        return MessageUtils.getColoredMessage(tagFormat.replace("{tag}", tag));
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
            return getGuiText("sort-role", lang, "&#FFD700Role");
        }
        return switch (type) {
            case ROLE -> getGuiText("sort-role", lang, "&#FFD700Role");
            case KDR -> getGuiText("sort-kdr", lang, "&#00FF88KDR");
            case PLAYTIME -> getGuiText("sort-playtime", lang, "&#00E5FFPlaytime");
            case JOIN_RECENT -> getGuiText("sort-join-recent", lang, "&#FFAA00Recent");
            case JOIN_OLDEST -> getGuiText("sort-join-oldest", lang, "&#E2E8F0Oldest");
        };
    }

    public String getTopSortTypeName(TopSortType type, String lang) {
        if (type == null) {
            return getGuiText("sort-top-kdr", lang, "&#00FF88KDR (Ratio)");
        }
        return switch (type) {
            case KDR -> getGuiText("sort-top-kdr", lang, "&#00FF88KDR (Ratio)");
            case KILLS -> getGuiText("sort-top-kills", lang, "&#FF3366Kills (Most Bloodthirsty)");
            case MEMBERS -> getGuiText("sort-top-members", lang, "&#00E5FFMembers (Largest)");
        };
    }

    public String resolveTopPlaceholder(String param, String lang) {
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
                    } else if (prop.equals("tag")) {
                        return topClan != null ? topClan.getTag() : getNoClanTag(lang);
                    } else if (prop.equals("tag_formatted")) {
                        return topClan != null ? formatTag(topClan.getTag()) : "";
                    } else if (prop.equals("leader")) {
                        if (topClan == null) return getNoneText(lang);
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

        if (param.endsWith("_en") || param.endsWith("_es")) {
            lang = param.substring(param.length() - 2);
            param = param.substring(0, param.length() - 3);
        } else if (param.startsWith("en_") || param.startsWith("es_")) {
            lang = param.substring(0, 2);
            param = param.substring(3);
        }

        if (param.startsWith("top_")) {
            return resolveTopPlaceholder(param, lang);
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

        if (cleanParam.equals("tag")) {
            return clan != null ? clan.getTag() : "";
        }

        if (cleanParam.equals("tag_formatted")) {
            return clan != null ? formatTag(clan.getTag()) : "";
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
            return cp != null ? getRoleFormatted(cp.getRole(), lang) : "";
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
                return getFriendlyFireBadge(false, lang);
            return getFriendlyFireBadge(clan.isFriendlyFire(), lang);
        }

        if (cleanParam.equals("ff_status")) {
            if (clan == null)
                return getFriendlyFireStatus(false, lang);
            return getFriendlyFireStatus(clan.isFriendlyFire(), lang);
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
                    return getFlagBadge(flag, val, lang);
                } else {
                    return getFlagStatus(flag, val, lang);
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

        return null;
    }
}
