package com.hazee.hyperbounty.manager;

import com.hazee.hyperbounty.HyperBounty;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {
    
    private final HyperBounty plugin;
    private final Map<UUID, Long> killCooldowns;
    private final Map<UUID, Long> rewardCooldowns;
    private final Map<UUID, Long> bountyCooldowns;
    
    public CooldownManager(HyperBounty plugin) {
        this.plugin = plugin;
        this.killCooldowns = new ConcurrentHashMap<>();
        this.rewardCooldowns = new ConcurrentHashMap<>();
        this.bountyCooldowns = new ConcurrentHashMap<>();
    }
    
    public boolean isOnKillCooldown(Player player) {
        if (player.hasPermission("hyperbounty.bypass")) return false;
        
        Long lastKill = killCooldowns.get(player.getUniqueId());
        if (lastKill == null) return false;
        
        long cooldown = plugin.getConfigManager().getLong("cooldowns.kill");
        return System.currentTimeMillis() - lastKill < cooldown;
    }
    
    public long getKillCooldownRemaining(Player player) {
        Long lastKill = killCooldowns.get(player.getUniqueId());
        if (lastKill == null) return 0;
        
        long cooldown = plugin.getConfigManager().getLong("cooldowns.kill");
        long remaining = cooldown - (System.currentTimeMillis() - lastKill);
        return Math.max(0, remaining);
    }
    
    public void setKillCooldown(Player player) {
        killCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    public boolean isOnRewardCooldown(Player player) {
        if (player.hasPermission("hyperbounty.bypass")) return false;
        
        Long lastReward = rewardCooldowns.get(player.getUniqueId());
        if (lastReward == null) return false;
        
        long cooldown = plugin.getConfigManager().getLong("cooldowns.reward");
        return System.currentTimeMillis() - lastReward < cooldown;
    }
    
    public long getRewardCooldownRemaining(Player player) {
        Long lastReward = rewardCooldowns.get(player.getUniqueId());
        if (lastReward == null) return 0;
        
        long cooldown = plugin.getConfigManager().getLong("cooldowns.reward");
        long remaining = cooldown - (System.currentTimeMillis() - lastReward);
        return Math.max(0, remaining);
    }
    
    public void setRewardCooldown(Player player) {
        rewardCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    public boolean isOnBountyCooldown(Player player) {
        if (player.hasPermission("hyperbounty.bypass")) return false;
        
        Long lastBounty = bountyCooldowns.get(player.getUniqueId());
        if (lastBounty == null) return false;
        
        long cooldown = plugin.getConfigManager().getLong("cooldowns.bounty");
        return System.currentTimeMillis() - lastBounty < cooldown;
    }
    
    public long getBountyCooldownRemaining(Player player) {
        Long lastBounty = bountyCooldowns.get(player.getUniqueId());
        if (lastBounty == null) return 0;
        
        long cooldown = plugin.getConfigManager().getLong("cooldowns.bounty");
        long remaining = cooldown - (System.currentTimeMillis() - lastBounty);
        return Math.max(0, remaining);
    }
    
    public void setBountyCooldown(Player player) {
        bountyCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    public void clearCooldowns(UUID playerUUID) {
        killCooldowns.remove(playerUUID);
        rewardCooldowns.remove(playerUUID);
        bountyCooldowns.remove(playerUUID);
    }
}