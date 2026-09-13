package org.zkaleejoo.database;

import org.zkaleejoo.MaxClans;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.models.ClanHome;
import org.zkaleejoo.models.ClanPlayer;
import org.zkaleejoo.models.ClanRole;
import org.zkaleejoo.utils.FoliaCompat;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class ClanStorage {

    private final MaxClans plugin;
    private final DatabaseManager databaseManager;
    private final AtomicInteger pendingTasks = new AtomicInteger(0);
    private final ExecutorService sqliteWriteExecutor;

    public ClanStorage(MaxClans plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        if (databaseManager.getType() == DatabaseManager.DatabaseType.SQLITE) {
            this.sqliteWriteExecutor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "MaxClans-SQLite-Writer");
                t.setDaemon(true);
                return t;
            });
        } else {
            this.sqliteWriteExecutor = null;
        }
    }

    public void runAsync(Runnable runnable) {
        if (!plugin.isEnabled()) {
            runnable.run();
            return;
        }
        pendingTasks.incrementAndGet();
        try {
            if (databaseManager.getType() == DatabaseManager.DatabaseType.SQLITE && sqliteWriteExecutor != null && !sqliteWriteExecutor.isShutdown()) {
                sqliteWriteExecutor.submit(() -> {
                    try {
                        runnable.run();
                    } finally {
                        pendingTasks.decrementAndGet();
                    }
                });
            } else {
                FoliaCompat.runAsync(plugin, () -> {
                    try {
                        runnable.run();
                    } finally {
                        pendingTasks.decrementAndGet();
                    }
                });
            }
        } catch (Throwable t) {
            pendingTasks.decrementAndGet();
            runnable.run();
        }
    }

    public void flushAndAwait(long timeoutMs) {
        long start = System.currentTimeMillis();
        while (pendingTasks.get() > 0 && (System.currentTimeMillis() - start) < timeoutMs) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void close() {
        if (sqliteWriteExecutor != null && !sqliteWriteExecutor.isShutdown()) {
            sqliteWriteExecutor.shutdown();
            try {
                if (!sqliteWriteExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                    sqliteWriteExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                sqliteWriteExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public void saveClan(Clan clan) {
        runAsync(() -> {
            String sql = "INSERT INTO clans (name, tag, display_name, owner, friendly_fire, open_join, ally_damage, member_invites, visible_in_list, public_home, spy_chat, created_at, kills, deaths, rival_kills, level, exp, bank_balance) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = databaseManager.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
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
                ps.setInt(17, clan.getExp());
                ps.setDouble(18, clan.getBankBalance());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save clan: " + clan.getName(), e);
            }
        });
    }

    public void deleteClan(String clanName) {
        runAsync(() -> {
            try (Connection conn = databaseManager.getConnection()) {
                try (PreparedStatement ps = conn
                        .prepareStatement("DELETE FROM clan_allies WHERE LOWER(clan_name) = ? OR LOWER(ally_name) = ?")) {
                    ps.setString(1, clanName.toLowerCase());
                    ps.setString(2, clanName.toLowerCase());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn
                        .prepareStatement("DELETE FROM clan_chests WHERE LOWER(clan_name) = ?")) {
                    ps.setString(1, clanName.toLowerCase());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn
                        .prepareStatement("DELETE FROM clan_quests WHERE LOWER(clan_name) = ?")) {
                    ps.setString(1, clanName.toLowerCase());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn
                        .prepareStatement("DELETE FROM clan_homes WHERE LOWER(clan_name) = ?")) {
                    ps.setString(1, clanName.toLowerCase());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn
                        .prepareStatement("DELETE FROM clan_players WHERE clan_name = ?")) {
                    ps.setString(1, clanName);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn
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
        runAsync(() -> {
            String sql = "UPDATE clans SET tag = ?, display_name = ?, owner = ?, friendly_fire = ?, open_join = ?, ally_damage = ?, member_invites = ?, visible_in_list = ?, public_home = ?, spy_chat = ?, kills = ?, deaths = ?, rival_kills = ?, level = ?, exp = ?, bank_balance = ? WHERE name = ?";
            try (Connection conn = databaseManager.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
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
                ps.setInt(15, clan.getExp());
                ps.setDouble(16, clan.getBankBalance());
                ps.setString(17, clan.getName());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to update clan: " + clan.getName(), e);
            }
        });
    }

    public void saveHome(String clanName, ClanHome home) {
        runAsync(() -> {
            try (Connection conn = databaseManager.getConnection()) {
                try (PreparedStatement del = conn
                        .prepareStatement("DELETE FROM clan_homes WHERE LOWER(clan_name) = ? AND LOWER(name) = ?")) {
                    del.setString(1, clanName.toLowerCase());
                    del.setString(2, home.getName().toLowerCase());
                    del.executeUpdate();
                }
                String sql = "INSERT INTO clan_homes (clan_name, name, world, x, y, z, yaw, pitch, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
        runAsync(() -> {
            String sql = "DELETE FROM clan_homes WHERE LOWER(clan_name) = ? AND LOWER(name) = ?";
            try (Connection conn = databaseManager.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
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
        runAsync(() -> {
            String sql = "INSERT INTO clan_players (uuid, clan_name, role, joined_at) VALUES (?, ?, ?, ?)";
            try (Connection conn = databaseManager.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
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
        runAsync(() -> {
            String sql = "DELETE FROM clan_players WHERE uuid = ?";
            try (Connection conn = databaseManager.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to remove clan player: " + uuid, e);
            }
        });
    }

    public void updateClanPlayerRole(UUID uuid, ClanRole role) {
        runAsync(() -> {
            String sql = "UPDATE clan_players SET role = ? WHERE uuid = ?";
            try (Connection conn = databaseManager.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
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

        try (Connection conn = databaseManager.getConnection()) {
            try (Statement stmt = conn.createStatement();
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
                    try {
                        clan.setExp(rs.getInt("exp"));
                    } catch (SQLException ignored) {
                    }
                    try {
                        clan.setBankBalance(rs.getDouble("bank_balance"));
                    } catch (SQLException ignored) {
                    }

                    clans.put(name.toLowerCase(), clan);
                }
            }

            try (Statement stmt = conn.createStatement();
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

            try (Statement stmt = conn.createStatement();
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

            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM clan_allies")) {

                while (rs.next()) {
                    String clanName = rs.getString("clan_name");
                    String allyName = rs.getString("ally_name");

                    Clan clan = clans.get(clanName.toLowerCase());
                    if (clan != null) {
                        clan.addAlly(allyName);
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load clan allies from database: " + e.getMessage());
            }

            plugin.getLogger().info("Loaded " + clans.size() + " clans from database.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load clans from database!", e);
        }

        return clans;
    }

    public void saveAlly(String clanName, String allyName) {
        runAsync(() -> {
            String sql = "INSERT INTO clan_allies (clan_name, ally_name) VALUES (?, ?)";
            try (Connection conn = databaseManager.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, clanName);
                ps.setString(2, allyName);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save clan ally: " + clanName + " <-> " + allyName, e);
            }
        });
    }

    public void removeAlly(String clanName, String allyName) {
        runAsync(() -> {
            String sql = "DELETE FROM clan_allies WHERE (LOWER(clan_name) = ? AND LOWER(ally_name) = ?) OR (LOWER(clan_name) = ? AND LOWER(ally_name) = ?)";
            try (Connection conn = databaseManager.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, clanName.toLowerCase());
                ps.setString(2, allyName.toLowerCase());
                ps.setString(3, allyName.toLowerCase());
                ps.setString(4, clanName.toLowerCase());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to remove clan ally: " + clanName + " <-> " + allyName, e);
            }
        });
    }

    public void saveClanChest(String clanName, String serializedData) {
        if (!plugin.isEnabled()) {
            saveClanChestSync(clanName, serializedData);
            return;
        }
        runAsync(() -> saveClanChestSync(clanName, serializedData));
    }

    public void saveClanChestSync(String clanName, String serializedData) {
        if (clanName == null || serializedData == null) {
            return;
        }
        String sql = "INSERT INTO clan_chests (clan_name, inventory_data, updated_at) VALUES (?, ?, ?) "
                + "ON CONFLICT(clan_name) DO UPDATE SET inventory_data = excluded.inventory_data, updated_at = excluded.updated_at";
        if (databaseManager.getType() == DatabaseManager.DatabaseType.MYSQL) {
            sql = "INSERT INTO clan_chests (clan_name, inventory_data, updated_at) VALUES (?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE inventory_data = VALUES(inventory_data), updated_at = VALUES(updated_at)";
        }
        try (Connection conn = databaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, clanName);
            ps.setString(2, serializedData);
            ps.setLong(3, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save clan chest for: " + clanName, e);
        }
    }

    public String loadClanChest(String clanName) {
        String sql = "SELECT inventory_data FROM clan_chests WHERE LOWER(clan_name) = ?";
        try (Connection conn = databaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, clanName.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("inventory_data");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load clan chest for: " + clanName, e);
        }
        return null;
    }

    public void saveQuestProgress(org.zkaleejoo.models.ClanQuestProgress progress) {
        runAsync(() -> {
            String sql = "INSERT INTO clan_quests (clan_name, quest_id, progress, completed, assigned_date) VALUES (?, ?, ?, ?, ?) "
                    + "ON CONFLICT(clan_name, quest_id, assigned_date) DO UPDATE SET progress = excluded.progress, completed = excluded.completed";
            if (databaseManager.getType() == DatabaseManager.DatabaseType.MYSQL) {
                sql = "INSERT INTO clan_quests (clan_name, quest_id, progress, completed, assigned_date) VALUES (?, ?, ?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE progress = VALUES(progress), completed = VALUES(completed)";
            }
            try (Connection conn = databaseManager.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, progress.getClanName());
                ps.setString(2, progress.getQuestId());
                ps.setInt(3, progress.getProgress());
                ps.setBoolean(4, progress.isCompleted());
                ps.setString(5, progress.getAssignedDate());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save clan quest progress: " + progress.getClanName() + " / " + progress.getQuestId(), e);
            }
        });
    }

    public Map<String, org.zkaleejoo.models.ClanQuestProgress> loadQuestProgressForClan(String clanName, String date) {
        Map<String, org.zkaleejoo.models.ClanQuestProgress> result = new HashMap<>();
        String sql = "SELECT quest_id, progress, completed, assigned_date FROM clan_quests WHERE LOWER(clan_name) = ? AND assigned_date = ?";
        try (Connection conn = databaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, clanName.toLowerCase());
            ps.setString(2, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String questId = rs.getString("quest_id");
                    int prog = rs.getInt("progress");
                    boolean comp = rs.getBoolean("completed");
                    String assignedDate = rs.getString("assigned_date");
                    result.put(questId, new org.zkaleejoo.models.ClanQuestProgress(clanName, questId, prog, comp, assignedDate));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load clan quest progress for: " + clanName, e);
        }
        return result;
    }
}
