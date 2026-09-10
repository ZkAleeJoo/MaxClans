package org.zkaleejoo.database;

import org.zkaleejoo.MaxClans;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class DatabaseManager {

    private final MaxClans plugin;
    private Connection connection;
    private DatabaseType type;

    public enum DatabaseType {
        SQLITE, MYSQL
    }

    public DatabaseManager(MaxClans plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        String dbType = plugin.getMainConfigManager().getDatabaseType();

        if ("mysql".equalsIgnoreCase(dbType)) {
            type = DatabaseType.MYSQL;
            connectMySQL();
        } else {
            type = DatabaseType.SQLITE;
            connectSQLite();
        }

        createTables();
    }

    private void connectSQLite() {
        try {
            File dbFile = new File(plugin.getDataFolder(), "clans.db");
            if (!dbFile.exists()) {
                plugin.getDataFolder().mkdirs();
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            plugin.getLogger().info("SQLite database connected successfully.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to SQLite database!", e);
        }
    }

    private void connectMySQL() {
        try {
            String host = plugin.getMainConfigManager().getDatabaseHost();
            int port = plugin.getMainConfigManager().getDatabasePort();
            String database = plugin.getMainConfigManager().getDatabaseName();
            String username = plugin.getMainConfigManager().getDatabaseUsername();
            String password = plugin.getMainConfigManager().getDatabasePassword();

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?useSSL=false&autoReconnect=true&useUnicode=true&characterEncoding=UTF-8";
            connection = DriverManager.getConnection(url, username, password);
            plugin.getLogger().info("MySQL database connected successfully.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to MySQL database! Falling back to SQLite.", e);
            type = DatabaseType.SQLITE;
            connectSQLite();
        }
    }

    private void createTables() {
        String engine = (type == DatabaseType.MYSQL) ? " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4" : "";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS clans ("
                    + "name VARCHAR(64) PRIMARY KEY,"
                    + "tag VARCHAR(64) NOT NULL,"
                    + "display_name VARCHAR(255) DEFAULT NULL,"
                    + "owner VARCHAR(36) NOT NULL,"
                    + "friendly_fire BOOLEAN DEFAULT 0,"
                    + "open_join BOOLEAN DEFAULT 0,"
                    + "ally_damage BOOLEAN DEFAULT 0,"
                    + "member_invites BOOLEAN DEFAULT 0,"
                    + "visible_in_list BOOLEAN DEFAULT 1,"
                    + "public_home BOOLEAN DEFAULT 0,"
                    + "spy_chat BOOLEAN DEFAULT 0,"
                    + "kills INT DEFAULT 0,"
                    + "deaths INT DEFAULT 0,"
                    + "rival_kills INT DEFAULT 0,"
                    + "level INT DEFAULT 1,"
                    + "exp INT DEFAULT 0,"
                    + "bank_balance DOUBLE DEFAULT 0.0,"
                    + "created_at BIGINT NOT NULL"
                    + ")" + engine);

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS clan_players ("
                    + "uuid VARCHAR(36) PRIMARY KEY,"
                    + "clan_name VARCHAR(64) NOT NULL,"
                    + "role VARCHAR(16) NOT NULL DEFAULT 'MEMBER',"
                    + "joined_at BIGINT NOT NULL DEFAULT 0,"
                    + "FOREIGN KEY (clan_name) REFERENCES clans(name) ON DELETE CASCADE"
                    + ")" + engine);

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS clan_homes ("
                    + "clan_name VARCHAR(64) NOT NULL,"
                    + "name VARCHAR(32) NOT NULL,"
                    + "world VARCHAR(64) NOT NULL,"
                    + "x DOUBLE NOT NULL,"
                    + "y DOUBLE NOT NULL,"
                    + "z DOUBLE NOT NULL,"
                    + "yaw FLOAT NOT NULL,"
                    + "pitch FLOAT NOT NULL,"
                    + "created_at BIGINT NOT NULL,"
                    + "PRIMARY KEY (clan_name, name),"
                    + "FOREIGN KEY (clan_name) REFERENCES clans(name) ON DELETE CASCADE"
                    + ")" + engine);

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS clan_chests ("
                    + "clan_name VARCHAR(64) PRIMARY KEY,"
                    + "inventory_data TEXT NOT NULL,"
                    + "updated_at BIGINT NOT NULL,"
                    + "FOREIGN KEY (clan_name) REFERENCES clans(name) ON DELETE CASCADE"
                    + ")" + engine);

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS clan_allies ("
                    + "clan_name VARCHAR(64) NOT NULL,"
                    + "ally_name VARCHAR(64) NOT NULL,"
                    + "PRIMARY KEY (clan_name, ally_name),"
                    + "FOREIGN KEY (clan_name) REFERENCES clans(name) ON DELETE CASCADE"
                    + ")" + engine);

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS clan_quests ("
                    + "clan_name VARCHAR(64) NOT NULL,"
                    + "quest_id VARCHAR(64) NOT NULL,"
                    + "progress INT DEFAULT 0,"
                    + "completed BOOLEAN DEFAULT 0,"
                    + "assigned_date VARCHAR(16) NOT NULL,"
                    + "PRIMARY KEY (clan_name, quest_id, assigned_date),"
                    + "FOREIGN KEY (clan_name) REFERENCES clans(name) ON DELETE CASCADE"
                    + ")" + engine);

            try {
                stmt.executeUpdate("ALTER TABLE clans ADD COLUMN level INT DEFAULT 1");
            } catch (SQLException ignored) {
            }
            try {
                stmt.executeUpdate("ALTER TABLE clans ADD COLUMN exp INT DEFAULT 0");
            } catch (SQLException ignored) {
            }
            try {
                stmt.executeUpdate("ALTER TABLE clans ADD COLUMN bank_balance DOUBLE DEFAULT 0.0");
            } catch (SQLException ignored) {
            }

            try {
                stmt.executeUpdate("ALTER TABLE clan_players ADD COLUMN joined_at BIGINT DEFAULT 0");
            } catch (SQLException ignored) {
            }
            try {
                stmt.executeUpdate("ALTER TABLE clans ADD COLUMN open_join BOOLEAN DEFAULT 0");
            } catch (SQLException ignored) {
            }
            try {
                stmt.executeUpdate("ALTER TABLE clans ADD COLUMN ally_damage BOOLEAN DEFAULT 0");
            } catch (SQLException ignored) {
            }
            try {
                stmt.executeUpdate("ALTER TABLE clans ADD COLUMN member_invites BOOLEAN DEFAULT 0");
            } catch (SQLException ignored) {
            }
            try {
                stmt.executeUpdate("ALTER TABLE clans ADD COLUMN visible_in_list BOOLEAN DEFAULT 1");
            } catch (SQLException ignored) {
            }
            try {
                stmt.executeUpdate("ALTER TABLE clans ADD COLUMN public_home BOOLEAN DEFAULT 0");
            } catch (SQLException ignored) {
            }
            try {
                stmt.executeUpdate("ALTER TABLE clans ADD COLUMN spy_chat BOOLEAN DEFAULT 0");
            } catch (SQLException ignored) {
            }
            try {
                stmt.executeUpdate("ALTER TABLE clans ADD COLUMN kills INT DEFAULT 0");
            } catch (SQLException ignored) {
            }
            try {
                stmt.executeUpdate("ALTER TABLE clans ADD COLUMN deaths INT DEFAULT 0");
            } catch (SQLException ignored) {
            }
            try {
                stmt.executeUpdate("ALTER TABLE clans ADD COLUMN rival_kills INT DEFAULT 0");
            } catch (SQLException ignored) {
            }
            try {
                stmt.executeUpdate("ALTER TABLE clans ADD COLUMN display_name VARCHAR(255) DEFAULT NULL");
            } catch (SQLException ignored) {
            }
            if (type == DatabaseType.MYSQL) {
                try {
                    stmt.executeUpdate("ALTER TABLE clans MODIFY COLUMN tag VARCHAR(64) NOT NULL");
                } catch (SQLException ignored) {
                }
            }

            plugin.getLogger().info("Database tables created/verified successfully.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create database tables!", e);
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                if (type == DatabaseType.MYSQL) {
                    connectMySQL();
                } else {
                    connectSQLite();
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to check database connection!", e);
        }
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Database connection closed.");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to close database connection!", e);
        }
    }

    public DatabaseType getType() {
        return type;
    }
}
