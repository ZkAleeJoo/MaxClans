package org.zkaleejoo.database;

import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.models.Clan;
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
            String sql = "INSERT INTO clans (name, tag, owner, friendly_fire, open_join, ally_damage, member_invites, visible_in_list, public_home, spy_chat, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = databaseManager.getConnection().prepareStatement(sql)) {
                ps.setString(1, clan.getName());
                ps.setString(2, clan.getTag());
                ps.setString(3, clan.getOwner().toString());
                ps.setBoolean(4, clan.isFriendlyFire());
                ps.setBoolean(5, clan.isOpenJoin());
                ps.setBoolean(6, clan.isAllyDamage());
                ps.setBoolean(7, clan.isMemberInvites());
                ps.setBoolean(8, clan.isVisibleInList());
                ps.setBoolean(9, clan.isPublicHome());
                ps.setBoolean(10, clan.isSpyChat());
                ps.setLong(11, clan.getCreatedAt());
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
            String sql = "UPDATE clans SET tag = ?, owner = ?, friendly_fire = ?, open_join = ?, ally_damage = ?, member_invites = ?, visible_in_list = ?, public_home = ?, spy_chat = ? WHERE name = ?";
            try (PreparedStatement ps = databaseManager.getConnection().prepareStatement(sql)) {
                ps.setString(1, clan.getTag());
                ps.setString(2, clan.getOwner().toString());
                ps.setBoolean(3, clan.isFriendlyFire());
                ps.setBoolean(4, clan.isOpenJoin());
                ps.setBoolean(5, clan.isAllyDamage());
                ps.setBoolean(6, clan.isMemberInvites());
                ps.setBoolean(7, clan.isVisibleInList());
                ps.setBoolean(8, clan.isPublicHome());
                ps.setBoolean(9, clan.isSpyChat());
                ps.setString(10, clan.getName());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to update clan: " + clan.getName(), e);
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

            plugin.getLogger().info("Loaded " + clans.size() + " clans from database.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load clans from database!", e);
        }

        return clans;
    }
}
