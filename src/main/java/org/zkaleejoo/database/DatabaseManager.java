package org.zkaleejoo.database;

import org.zkaleejoo.OnlyClans;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class DatabaseManager {

    private final OnlyClans plugin;
    private Connection connection;
    private DatabaseType type;

    public enum DatabaseType {
        SQLITE, MYSQL
    }

    public DatabaseManager(OnlyClans plugin) {
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
        String autoIncrement = (type == DatabaseType.MYSQL) ? "AUTO_INCREMENT" : "AUTOINCREMENT";
        String engine = (type == DatabaseType.MYSQL) ? " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4" : "";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS clans ("
                    + "name VARCHAR(64) PRIMARY KEY,"
                    + "tag VARCHAR(16) NOT NULL,"
                    + "owner VARCHAR(36) NOT NULL,"
                    + "friendly_fire BOOLEAN DEFAULT 0,"
                    + "created_at BIGINT NOT NULL"
                    + ")" + engine);

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS clan_players ("
                    + "uuid VARCHAR(36) PRIMARY KEY,"
                    + "clan_name VARCHAR(64) NOT NULL,"
                    + "role VARCHAR(16) NOT NULL DEFAULT 'MEMBER',"
                    + "FOREIGN KEY (clan_name) REFERENCES clans(name) ON DELETE CASCADE"
                    + ")" + engine);

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
