package com.hazee.hyperbounty.database;

import com.hazee.hyperbounty.HyperBounty;
import com.hazee.hyperbounty.model.BountyEntry;
import com.hazee.hyperbounty.model.Killstreak;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {
    
    private final HyperBounty plugin;
    
    public DatabaseManager(HyperBounty plugin) {
        this.plugin = plugin;
    }
    
    public void initialize() {
        // Simple initialization - chỉ tạo tables nếu dùng SQL
        createTables();
    }
    
    private void createTables() {
        // Tạo tables nếu dùng database
        // Đơn giản hóa: chỉ log
        plugin.getLogger().info("Database initialized");
    }
    
    public void saveBounty(BountyEntry bounty) {
        // Simple sync save
        // Trong thực tế sẽ save vào database
        plugin.getLogger().info("Bounty saved for " + bounty.getTargetName());
    }
    
    public List<BountyEntry> getActiveBounties() {
        // Simple sync get
        // Trong thực tế sẽ query từ database
        return new ArrayList<>();
    }
    
    public BountyEntry getBounty(UUID targetUUID) {
        // Simple sync get
        // Trong thực tế sẽ query từ database
        return null;
    }
    
    public void saveKillstreak(Killstreak killstreak) {
        // Simple sync save
        plugin.getLogger().info("Killstreak saved for " + killstreak.getPlayerName());
    }
    
    public Killstreak getKillstreak(UUID playerUUID) {
        // Simple sync get
        return null;
    }
    
    public void close() {
        // Close database connections if any
    }
    
    public boolean isUsingDatabase() {
        return false; // Simple mode
    }
}
