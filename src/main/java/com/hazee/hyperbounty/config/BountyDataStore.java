package com.hazee.hyperbounty.config;

import com.hazee.hyperbounty.HyperBounty;
import com.hazee.hyperbounty.model.BountyEntry;
import com.hazee.hyperbounty.model.Killstreak;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class BountyDataStore {
    
    private final HyperBounty plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    
    // Cache for better performance
    private final Map<UUID, BountyEntry> bountyCache;
    private final Map<UUID, Killstreak> killstreakCache;
    private final Map<UUID, CooldownData> cooldownCache;
    
    public BountyDataStore(HyperBounty plugin) {
        this.plugin = plugin;
        this.bountyCache = new HashMap<>();
        this.killstreakCache = new HashMap<>();
        this.cooldownCache = new HashMap<>();
    }
    
    public void initialize() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        dataFile = new File(plugin.getDataFolder(), "bounties.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create bounties.yml", e);
            }
        }
        
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        loadAllData();
    }
    
    private void loadAllData() {
        loadBounties();
        loadKillstreaks();
        loadCooldowns();
    }
    
    private void loadBounties() {
        bountyCache.clear();
        if (!dataConfig.isConfigurationSection("bounties")) {
            return;
        }
        
        for (String key : dataConfig.getConfigurationSection("bounties").getKeys(false)) {
            String path = "bounties." + key;
            
            try {
                UUID targetUUID = UUID.fromString(dataConfig.getString(path + ".targetUUID"));
                String targetName = dataConfig.getString(path + ".targetName");
                UUID setterUUID = UUID.fromString(dataConfig.getString(path + ".setterUUID"));
                String setterName = dataConfig.getString(path + ".setterName");
                double amount = dataConfig.getDouble(path + ".amount");
                boolean completed = dataConfig.getBoolean(path + ".completed", false);
                
                BountyEntry bounty = new BountyEntry(targetUUID, targetName, setterUUID, setterName, amount);
                bounty.setCompleted(completed);
                
                if (dataConfig.contains(path + ".hunterUUID")) {
                    bounty.setHunterUUID(UUID.fromString(dataConfig.getString(path + ".hunterUUID")));
                }
                if (dataConfig.contains(path + ".hunterName")) {
                    bounty.setHunterName(dataConfig.getString(path + ".hunterName"));
                }
                if (dataConfig.contains(path + ".completedAt")) {
                    bounty.setCompletedAt(dataConfig.getLong(path + ".completedAt"));
                }
                
                if (!completed) {
                    bountyCache.put(targetUUID, bounty);
                }
                
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in bounty data: " + key);
            }
        }
        
        plugin.getLogger().info("Loaded " + bountyCache.size() + " active bounties from file");
    }
    
    private void loadKillstreaks() {
        killstreakCache.clear();
        if (!dataConfig.isConfigurationSection("killstreaks")) {
            return;
        }
        
        for (String key : dataConfig.getConfigurationSection("killstreaks").getKeys(false)) {
            String path = "killstreaks." + key;
            
            try {
                UUID playerUUID = UUID.fromString(key);
                String playerName = dataConfig.getString(path + ".playerName");
                int streak = dataConfig.getInt(path + ".streak");
                long lastKill = dataConfig.getLong(path + ".lastKill");
                
                Killstreak killstreak = new Killstreak(playerUUID, playerName, streak, lastKill);
                killstreakCache.put(playerUUID, killstreak);
                
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in killstreak data: " + key);
            }
        }
        
        plugin.getLogger().info("Loaded " + killstreakCache.size() + " killstreaks from file");
    }
    
    private void loadCooldowns() {
        cooldownCache.clear();
        if (!dataConfig.isConfigurationSection("cooldowns")) {
            return;
        }
        
        for (String key : dataConfig.getConfigurationSection("cooldowns").getKeys(false)) {
            String path = "cooldowns." + key;
            
            try {
                UUID playerUUID = UUID.fromString(key);
                long lastKill = dataConfig.getLong(path + ".lastKill", 0);
                long lastReward = dataConfig.getLong(path + ".lastReward", 0);
                long lastBounty = dataConfig.getLong(path + ".lastBounty", 0);
                
                CooldownData cooldownData = new CooldownData(lastKill, lastReward, lastBounty);
                cooldownCache.put(playerUUID, cooldownData);
                
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in cooldown data: " + key);
            }
        }
        
        plugin.getLogger().info("Loaded " + cooldownCache.size() + " cooldown entries from file");
    }
    
    public CompletableFuture<Void> saveBounty(BountyEntry bounty) {
        return CompletableFuture.runAsync(() -> {
            String path = "bounties." + bounty.getTargetUUID().toString();
            
            dataConfig.set(path + ".targetUUID", bounty.getTargetUUID().toString());
            dataConfig.set(path + ".targetName", bounty.getTargetName());
            dataConfig.set(path + ".setterUUID", bounty.getSetterUUID().toString());
            dataConfig.set(path + ".setterName", bounty.getSetterName());
            dataConfig.set(path + ".amount", bounty.getAmount());
            dataConfig.set(path + ".completed", bounty.isCompleted());
            
            if (bounty.getHunterUUID() != null) {
                dataConfig.set(path + ".hunterUUID", bounty.getHunterUUID().toString());
            }
            if (bounty.getHunterName() != null) {
                dataConfig.set(path + ".hunterName", bounty.getHunterName());
            }
            if (bounty.getCompletedAt() > 0) {
                dataConfig.set(path + ".completedAt", bounty.getCompletedAt());
            }
            
            if (bounty.isCompleted()) {
                bountyCache.remove(bounty.getTargetUUID());
            } else {
                bountyCache.put(bounty.getTargetUUID(), bounty);
            }
            
            saveConfig();
        });
    }
    
    public CompletableFuture<List<BountyEntry>> getActiveBounties() {
        return CompletableFuture.supplyAsync(() -> new ArrayList<>(bountyCache.values()));
    }
    
    public CompletableFuture<BountyEntry> getBounty(UUID targetUUID) {
        return CompletableFuture.supplyAsync(() -> bountyCache.get(targetUUID));
    }
    
    public CompletableFuture<Void> removeBounty(UUID targetUUID) {
        return CompletableFuture.runAsync(() -> {
            String path = "bounties." + targetUUID.toString();
            dataConfig.set(path, null);
            bountyCache.remove(targetUUID);
            saveConfig();
        });
    }
    
    public CompletableFuture<Void> saveKillstreak(Killstreak killstreak) {
        return CompletableFuture.runAsync(() -> {
            String path = "killstreaks." + killstreak.getPlayerUUID().toString();
            
            dataConfig.set(path + ".playerName", killstreak.getPlayerName());
            dataConfig.set(path + ".streak", killstreak.getStreak());
            dataConfig.set(path + ".lastKill", killstreak.getLastKill());
            
            killstreakCache.put(killstreak.getPlayerUUID(), killstreak);
            saveConfig();
        });
    }
    
    public CompletableFuture<Killstreak> getKillstreak(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> killstreakCache.get(playerUUID));
    }
    
    public CompletableFuture<List<Killstreak>> getAllKillstreaks() {
        return CompletableFuture.supplyAsync(() -> new ArrayList<>(killstreakCache.values()));
    }
    
    public CompletableFuture<Void> saveCooldown(UUID playerUUID, CooldownData cooldownData) {
        return CompletableFuture.runAsync(() -> {
            String path = "cooldowns." + playerUUID.toString();
            
            dataConfig.set(path + ".lastKill", cooldownData.getLastKill());
            dataConfig.set(path + ".lastReward", cooldownData.getLastReward());
            dataConfig.set(path + ".lastBounty", cooldownData.getLastBounty());
            
            cooldownCache.put(playerUUID, cooldownData);
            saveConfig();
        });
    }
    
    public CompletableFuture<CooldownData> getCooldown(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> cooldownCache.get(playerUUID));
    }
    
    public void saveConfig() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save bounties.yml", e);
        }
    }
    
    public void reloadData() {
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        loadAllData();
    }
    
    public int getBountyCount() {
        return bountyCache.size();
    }
    
    public int getKillstreakCount() {
        return killstreakCache.size();
    }
    
    public int getCooldownCount() {
        return cooldownCache.size();
    }
    
    // Cooldown data container class
    public static class CooldownData {
        private final long lastKill;
        private final long lastReward;
        private final long lastBounty;
        
        public CooldownData(long lastKill, long lastReward, long lastBounty) {
            this.lastKill = lastKill;
            this.lastReward = lastReward;
            this.lastBounty = lastBounty;
        }
        
        public long getLastKill() { return lastKill; }
        public long getLastReward() { return lastReward; }
        public long getLastBounty() { return lastBounty; }
        
        public CooldownData withLastKill(long lastKill) {
            return new CooldownData(lastKill, this.lastReward, this.lastBounty);
        }
        
        public CooldownData withLastReward(long lastReward) {
            return new CooldownData(this.lastKill, lastReward, this.lastBounty);
        }
        
        public CooldownData withLastBounty(long lastBounty) {
            return new CooldownData(this.lastKill, this.lastReward, lastBounty);
        }
    }
}