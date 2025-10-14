package com.hazee.hyperbounty.database;

import com.hazee.hyperbounty.HyperBounty;
import com.hazee.hyperbounty.model.BountyEntry;
import com.hazee.hyperbounty.model.Killstreak;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {
    
    private final HyperBounty plugin;
    
    public DatabaseManager(HyperBounty plugin) {
        this.plugin = plugin;
    }
    
    public void initialize() {
        plugin.getLogger().info("Database initialized");
    }
    
    public void saveBounty(BountyEntry bounty) {
        plugin.getLogger().info("Bounty saved for " + bounty.getTargetName());
    }
    
    public List<BountyEntry> getActiveBounties() {
        return new ArrayList<>();
    }
    
    public BountyEntry getBounty(UUID targetUUID) {
        return null;
    }
    
    public void saveKillstreak(Killstreak killstreak) {
        plugin.getLogger().info("Killstreak saved for " + killstreak.getPlayerName());
    }
    
    public Killstreak getKillstreak(UUID playerUUID) {
        return null;
    }
    
    public void close() {
    }
}
