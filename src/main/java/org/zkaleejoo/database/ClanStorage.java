package org.zkaleejoo.database;

import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.models.ClanHome;
import org.zkaleejoo.models.ClanPlayer;
import org.zkaleejoo.models.ClanRole;
import org.zkaleejoo.utils.FoliaCompat;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class ClanStorage {

    private final OnlyClans plugin;
    private final DatabaseManager databaseManager;

    public ClanStorage(OnlyClans plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    public void saveClan(Clan clan) {
        FoliaCompat.runAsync(plugin, () -> {
            String sql = "INSERT INTO clans (name, tag, display_name, owner, friendly_fire, open_join, ally_damage, member_invites, visible_in_list, public_home, spy_chat, created_at, kills, deaths, rival_kills, level) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = databaseManager.getConnection().prepareStatement(sql)) {
                ps.setString(1, clan.getName());
                ps.setString(2, clan.getTag());
                ps.setString(3, clan.getRawDisplayName());
                ps.setString(4, clan.getOwner().toString());
                ps.setBoolean(5, clan.isFriendlyFire());
                ps.setBoolean(6, clan.isOpenJoin());
                ps.setBoolean(7, clan.isAllyDamage());
                ps.setBoolean(8, clan.isMemberInvites());
                ps.setBoolean(9, clan.isVisibleInList());
                ps.setBoolean(10, clan.isPublicHome());
                ps.setBoolean(11, clan.isSpyChat());
                ps.setLong(12, clan.getCreatedAt());
                ps.setInt(13, clan.getKills());
                ps.setInt(14, clan.getDeaths());
                ps.setInt(15, clan.getRivalKills());
                ps.setInt(16, clan.getLevel());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save clan: " + clan.getName(), e);
            }
        });
    }

    public void deleteClan(String clanName) {
        FoliaCompat.runAsync(plugin, () -> {
            try {
                try (PreparedStatement ps = databaseManager.getConnection()
                        .prepareStatement("DELETE FROM clan_homes WHERE LOWER(clan_name) = ?")) {
                    ps.setString(1, clanName.toLowerCase());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = databaseManager.getConnection()
                        .prepareStatement("DELETE FROM clan_players WHERE clan_name = ?")) {
                    ps.setString(1, clanName);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = databaseManager.getConnection()
                        .prepareStatement("DELETE FROM clans WHERE name = ?")) {
                    ps.setString(1, clanName);
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to delete clan: " + clanName, e);
            }
        });
    }

    public void updateClan(Clan clan) {
        FoliaCompat.runAsync(plugin, () -> {
            String sql = "UPDATE clans SET tag = ?, display_name = ?, owner = ?, friendly_fire = ?, open_join = ?, ally_damage = ?, member_invites = ?, visible_in_list = ?, public_home = ?, spy_chat = ?, kills = ?, deaths = ?, rival_kills = ?, level = ? WHERE name = ?";
            try (PreparedStatement ps = databaseManager.getConnection().prepareStatement(sql)) {
                ps.setString(1, clan.getTag());
                ps.setString(2, clan.getRawDisplayName());
                ps.setString(3, clan.getOwner().toString());
                ps.setBoolean(4, clan.isFriendlyFire());
                ps.setBoolean(5, clan.isOpenJoin());
                ps.setBoolean(6, clan.isAllyDamage());
                ps.setBoolean(7, clan.isMemberInvites());
                ps.setBoolean(8, clan.isVisibleInList());
                ps.setBoolean(9, clan.isPublicHome());
                ps.setBoolean(10, clan.isSpyChat());
                ps.setInt(11, clan.getKills());
                ps.setInt(12, clan.getDeaths());
                ps.setInt(13, clan.getRivalKills());
                ps.setInt(14, clan.getLevel());
                ps.setString(15, clan.getName());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to update clan: " + clan.getName(), e);
            }
        });
    }

    public void saveHome(String clanName, ClanHome home) {
        FoliaCompat.runAsync(plugin, () -> {
            try {
                try (PreparedStatement del = databaseManager.getConnection()
                        .prepareStatement("DELETE FROM clan_homes WHERE LOWER(clan_name) = ? AND LOWER(name) = ?")) {
                    del.setString(1, clanName.toLowerCase());
                    del.setString(2, home.getName().toLowerCase());
                    del.executeUpdate();
                }
                String sql = "INSERT INTO clan_homes (clan_name, name, world, x, y, z, yaw, pitch, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = databaseManager.getConnection().prepareStatement(sql)) {
                    ps.setString(1, clanName);
                    ps.setString(2, home.getName());
                    ps.setString(3, home.getWorldName());
                    ps.setDouble(4, home.getX());
                    ps.setDouble(5, home.getY());
                    ps.setDouble(6, home.getZ());
                    ps.setFloat(7, home.getYaw());
                    ps.setFloat(8, home.getPitch());
                    ps.setLong(9, home.getCreatedAt());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE,
                        "Failed to save clan home: " + home.getName() + " for clan " + clanName, e);
            }
        });
    }

    public void deleteHome(String clanName, String homeName) {
        FoliaCompat.runAsync(plugin, () -> {
            String sql = "DELETE FROM clan_homes WHERE LOWER(clan_name) = ? AND LOWER(name) = ?";
            try (PreparedStatement ps = databaseManager.getConnection().prepareStatement(sql)) {
                ps.setString(1, clanName.toLowerCase());
                ps.setString(2, homeName.toLowerCase());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE,
                        "Failed to delete clan home: " + homeName + " for clan " + clanName, e);
            }
        });
    }

    public void saveClanPlayer(ClanPlayer clanPlayer) {
        FoliaCompat.runAsync(plugin, () -> {
            String sql = "INSERT INTO clan_players (uuid, clan_name, role, joined_at) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = databaseManager.getConnection().prepareStatement(sql)) {
                ps.setString(1, clanPlayer.getUuid().toString());
                ps.setString(2, clanPlayer.getClanName());
                ps.setString(3, clanPlayer.getRole().name());
                ps.setLong(4, clanPlayer.getJoinedAt());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save clan player: " + clanPlayer.getUuid(), e);
            }
        });
    }

    public void removeClanPlayer(UUID uuid) {
        FoliaCompat.runAsync(plugin, () -> {
            String sql = "DELETE FROM clan_players WHERE uuid = ?";
            try (PreparedStatement ps = databaseManager.getConnection().prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to remove clan player: " + uuid, e);
            }
        });
    }

    public void updateClanPlayerRole(UUID uuid, ClanRole role) {
        FoliaCompat.runAsync(plugin, () -> {
            String sql = "UPDATE clan_players SET role = ? WHERE uuid = ?";
            try (PreparedStatement ps = databaseManager.getConnection().prepareStatement(sql)) {
                ps.setString(1, role.name());
                ps.setString(2, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to update player role: " + uuid, e);
            }
        });
    }

    public Map<String, Clan> loadAllClans() {
        Map<String, Clan> clans = new HashMap<>();

        try {
            try (Statement stmt = databaseManager.getConnection().createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM clans")) {

                while (rs.next()) {
                    String name = rs.getString("name");
                    String tag = rs.getString("tag");
                    UUID owner = UUID.fromString(rs.getString("owner"));
                    boolean friendlyFire = rs.getBoolean("friendly_fire");
                    long createdAt = rs.getLong("created_at");

                    Clan clan = new Clan(name, tag, owner, friendlyFire, createdAt);
                    try {
                        clan.setDisplayName(rs.getString("display_name"));
                    } catch (SQLException ignored) {
                    }
                    try {
                        clan.setOpenJoin(rs.getBoolean("open_join"));
                    } catch (SQLException ignored) {
                    }
                    try {
                        clan.setAllyDamage(rs.getBoolean("ally_damage"));
                    } catch (SQLException ignored) {
                    }
                    try {
                        clan.setMemberInvites(rs.getBoolean("member_invites"));
                    } catch (SQLException ignored) {
                    }
                    try {
                        clan.setVisibleInList(rs.getBoolean("visible_in_list"));
                    } catch (SQLException ignored) {
                    }
                    try {
                        clan.setPublicHome(rs.getBoolean("public_home"));
                    } catch (SQLException ignored) {
                    }
                    try {
                        clan.setSpyChat(rs.getBoolean("spy_chat"));
                    } catch (SQLException ignored) {
                    }
                    try {
                        clan.setKills(rs.getInt("kills"));
                    } catch (SQLException ignored) {
                    }
                    try {
                        clan.setDeaths(rs.getInt("deaths"));
                    } catch (SQLException ignored) {
                    }
                    try {
                        clan.setRivalKills(rs.getInt("rival_kills"));
                    } catch (SQLException ignored) {
                    }
                    try {
                        clan.setLevel(rs.getInt("level"));
                    } catch (SQLException ignored) {
                    }

                    clans.put(name.toLowerCase(), clan);
                }
            }

            try (Statement stmt = databaseManager.getConnection().createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM clan_players")) {

                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    String clanName = rs.getString("clan_name");
                    ClanRole role = ClanRole.valueOf(rs.getString("role"));
                    long joinedAt = 0;
                    try {
                        joinedAt = rs.getLong("joined_at");
                    } catch (SQLException ignored) {
                    }

                    Clan clan = clans.get(clanName.toLowerCase());
                    if (clan != null) {
                        if (joinedAt <= 0) {
                            joinedAt = clan.getCreatedAt();
                        }
                        ClanPlayer cp = new ClanPlayer(uuid, clanName, role, joinedAt);
                        clan.addMember(cp);
                    }
                }
            }

            try (Statement stmt = databaseManager.getConnection().createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM clan_homes")) {

                while (rs.next()) {
                    String clanName = rs.getString("clan_name");
                    String homeName = rs.getString("name");
                    String world = rs.getString("world");
                    double x = rs.getDouble("x");
                    double y = rs.getDouble("y");
                    double z = rs.getDouble("z");
                    float yaw = rs.getFloat("yaw");
                    float pitch = rs.getFloat("pitch");
                    long createdAt = rs.getLong("created_at");

                    Clan clan = clans.get(clanName.toLowerCase());
                    if (clan != null) {
                        ClanHome home = new ClanHome(homeName, world, x, y, z, yaw, pitch, createdAt);
                        clan.setHome(home);
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load clan homes from database: " + e.getMessage());
            }

            plugin.getLogger().info("Loaded " + clans.size() + " clans from database.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load clans from database!", e);
        }

        return clans;
    }
}
