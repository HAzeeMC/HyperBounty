package com.hazee.hyperbounty.database;

import com.hazee.hyperbounty.HyperBounty;
import com.hazee.hyperbounty.config.BountyDataStore;
import com.hazee.hyperbounty.model.BountyEntry;
import com.hazee.hyperbounty.model.Killstreak;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {
    
    private final HyperBounty plugin;
    private final BountyDataStore dataStore;
    private HikariDataSource dataSource;
    private String databaseType;
    private boolean useDatabase;
    
    public DatabaseManager(HyperBounty plugin) {
        this.plugin = plugin;
        this.dataStore = new BountyDataStore(plugin);
    }
    
    public void initialize() {
        String storageType = plugin.getConfigManager().getString("database.type", "SQLITE").toUpperCase();
        
        if (storageType.equals("YAML") || storageType.equals("FILE")) {
            useDatabase = false;
            dataStore.initialize();
            plugin.getLogger().info("Using YAML file storage for data");
            return;
        }
        
        useDatabase = true;
        databaseType = storageType;
        
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        switch (databaseType) {
            case "MYSQL":
                setupMySQL(config);
                break;
            case "SQLITE":
            default:
                setupSQLite(config);
                break;
        }
        
        dataSource = new HikariDataSource(config);
        createTables();
    }
    
    private void setupMySQL(HikariConfig config) {
        String host = plugin.getConfigManager().getString("database.host");
        int port = plugin.getConfigManager().getInt("database.port");
        String database = plugin.getConfigManager().getString("database.name");
        String username = plugin.getConfigManager().getString("database.username");
        String password = plugin.getConfigManager().getString("database.password");
        
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
    }
    
    private void setupSQLite(HikariConfig config) {
        config.setJdbcUrl("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/bounties.db");
        config.setDriverClassName("org.sqlite.JDBC");
    }
    
    private void createTables() {
        if (!useDatabase) return;
        
        String createBountiesTable = "CREATE TABLE IF NOT EXISTS bounties (" +
                "id INTEGER PRIMARY KEY " + (databaseType.equals("MYSQL") ? "AUTO_INCREMENT" : "AUTOINCREMENT") + "," +
                "target_uuid VARCHAR(36) NOT NULL," +
                "target_name VARCHAR(16) NOT NULL," +
                "setter_uuid VARCHAR(36) NOT NULL," +
                "setter_name VARCHAR(16) NOT NULL," +
                "amount DOUBLE NOT NULL," +
                "created_at BIGINT NOT NULL," +
                "hunter_uuid VARCHAR(36)," +
                "hunter_name VARCHAR(16)," +
                "completed BOOLEAN DEFAULT FALSE," +
                "completed_at BIGINT" +
                ");";
        
        String createKillstreaksTable = "CREATE TABLE IF NOT EXISTS killstreaks (" +
                "player_uuid VARCHAR(36) PRIMARY KEY," +
                "player_name VARCHAR(16) NOT NULL," +
                "killstreak INTEGER DEFAULT 0," +
                "last_kill BIGINT NOT NULL" +
                ");";
        
        String createCooldownsTable = "CREATE TABLE IF NOT EXISTS cooldowns (" +
                "player_uuid VARCHAR(36) PRIMARY KEY," +
                "last_kill BIGINT DEFAULT 0," +
                "last_reward BIGINT DEFAULT 0," +
                "last_bounty BIGINT DEFAULT 0" +
                ");";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(createBountiesTable);
            stmt.execute(createKillstreaksTable);
            stmt.execute(createCooldownsTable);
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create database tables: " + e.getMessage());
        }
    }
    
    public CompletableFuture<Void> saveBounty(BountyEntry bounty) {
        if (!useDatabase) {
            return dataStore.saveBounty(bounty);
        }
        
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO bounties (target_uuid, target_name, setter_uuid, setter_name, amount, created_at) VALUES (?, ?, ?, ?, ?, ?)";
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, bounty.getTargetUUID().toString());
                stmt.setString(2, bounty.getTargetName());
                stmt.setString(3, bounty.getSetterUUID().toString());
                stmt.setString(4, bounty.getSetterName());
                stmt.setDouble(5, bounty.getAmount());
                stmt.setLong(6, System.currentTimeMillis());
                
                stmt.executeUpdate();
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save bounty: " + e.getMessage());
            }
        });
    }
    
    public CompletableFuture<List<BountyEntry>> getActiveBounties() {
        if (!useDatabase) {
            return dataStore.getActiveBounties();
        }
        
        return CompletableFuture.supplyAsync(() -> {
            List<BountyEntry> bounties = new ArrayList<>();
            String sql = "SELECT * FROM bounties WHERE completed = FALSE ORDER BY amount DESC";
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    BountyEntry bounty = new BountyEntry(
                            UUID.fromString(rs.getString("target_uuid")),
                            rs.getString("target_name"),
                            UUID.fromString(rs.getString("setter_uuid")),
                            rs.getString("setter_name"),
                            rs.getDouble("amount")
                    );
                    bounties.add(bounty);
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get active bounties: " + e.getMessage());
            }
            
            return bounties;
        });
    }
    
    public CompletableFuture<BountyEntry> getBounty(UUID targetUUID) {
        if (!useDatabase) {
            return dataStore.getBounty(targetUUID);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM bounties WHERE target_uuid = ? AND completed = FALSE";
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, targetUUID.toString());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return new BountyEntry(
                            UUID.fromString(rs.getString("target_uuid")),
                            rs.getString("target_name"),
                            UUID.fromString(rs.getString("setter_uuid")),
                            rs.getString("setter_name"),
                            rs.getDouble("amount")
                    );
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get bounty: " + e.getMessage());
            }
            
            return null;
        });
    }
    
    // Additional methods for killstreaks and cooldowns...
    public CompletableFuture<Void> saveKillstreak(Killstreak killstreak) {
        if (!useDatabase) {
            return dataStore.saveKillstreak(killstreak);
        }
        
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT OR REPLACE INTO killstreaks (player_uuid, player_name, killstreak, last_kill) VALUES (?, ?, ?, ?)";
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, killstreak.getPlayerUUID().toString());
                stmt.setString(2, killstreak.getPlayerName());
                stmt.setInt(3, killstreak.getStreak());
                stmt.setLong(4, killstreak.getLastKill());
                
                stmt.executeUpdate();
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save killstreak: " + e.getMessage());
            }
        });
    }
    
    public CompletableFuture<Killstreak> getKillstreak(UUID playerUUID) {
        if (!useDatabase) {
            return dataStore.getKillstreak(playerUUID);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM killstreaks WHERE player_uuid = ?";
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, playerUUID.toString());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return new Killstreak(
                            UUID.fromString(rs.getString("player_uuid")),
                            rs.getString("player_name"),
                            rs.getInt("killstreak"),
                            rs.getLong("last_kill")
                    );
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get killstreak: " + e.getMessage());
            }
            
            return null;
        });
    }
    
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
    
    public boolean isUsingDatabase() {
        return useDatabase;
    }
    
    public BountyDataStore getDataStore() {
        return dataStore;
    }
}