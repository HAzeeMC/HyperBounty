package com.hazee.hyperbounty.manager;

import com.hazee.hyperbounty.HyperBounty;
import com.hazee.hyperbounty.model.BountyEntry;
import com.hazee.hyperbounty.utils.SchedulerUtil;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class BountyManager {
    
    private final HyperBounty plugin;
    private final Map<UUID, BountyEntry> activeBounties;
    private final Map<UUID, Set<UUID>> hunterMissions;
    
    public BountyManager(HyperBounty plugin) {
        this.plugin = plugin;
        this.activeBounties = new ConcurrentHashMap<>();
        this.hunterMissions = new ConcurrentHashMap<>();
        loadBounties();
    }
    
    private void loadBounties() {
        plugin.getDatabaseManager().getActiveBounties().thenAccept(bounties -> {
            SchedulerUtil.runTask(plugin, () -> {
                for (BountyEntry bounty : bounties) {
                    activeBounties.put(bounty.getTargetUUID(), bounty);
                }
                plugin.getLogger().info("Loaded " + bounties.size() + " active bounties");
            });
        });
    }
    
    public CompletableFuture<Boolean> setBounty(Player setter, Player target, double amount) {
        return CompletableFuture.supplyAsync(() -> {
            double minAmount = plugin.getConfigManager().getDouble("bounty.min-amount");
            double maxAmount = plugin.getConfigManager().getDouble("bounty.max-amount");
            
            if (amount < minAmount) {
                SchedulerUtil.runTask(plugin, setter, () -> 
                    plugin.getMessageManager().sendMessage(setter, "bounty.amount-too-low",
                            Map.of("min_amount", String.valueOf(minAmount))));
                return false;
            }
            
            if (amount > maxAmount) {
                SchedulerUtil.runTask(plugin, setter, () -> 
                    plugin.getMessageManager().sendMessage(setter, "bounty.amount-too-high",
                            Map.of("max_amount", String.valueOf(maxAmount))));
                return false;
            }
            
            if (!plugin.getEconomyHook().has(setter, amount)) {
                SchedulerUtil.runTask(plugin, setter, () -> 
                    plugin.getMessageManager().sendMessage(setter, "bounty.insufficient-funds"));
                return false;
            }
            
            // Check if target already has a bounty
            BountyEntry existingBounty = activeBounties.get(target.getUniqueId());
            if (existingBounty != null) {
                SchedulerUtil.runTask(plugin, setter, () -> 
                    plugin.getMessageManager().sendMessage(setter, "bounty.already-exists",
                            Map.of("target", target.getName())));
                return false;
            }
            
            // Create new bounty
            BountyEntry bounty = new BountyEntry(
                    target.getUniqueId(),
                    target.getName(),
                    setter.getUniqueId(),
                    setter.getName(),
                    amount
            );
            
            // Charge the setter
            plugin.getEconomyHook().withdrawPlayer(setter, amount);
            
            // Save to database and cache
            plugin.getDatabaseManager().saveBounty(bounty).join();
            activeBounties.put(target.getUniqueId(), bounty);
            
            // Broadcast message
            Map<String, String> placeholders = Map.of(
                    "setter", setter.getName(),
                    "target", target.getName(),
                    "amount", plugin.getEconomyHook().format(amount)
            );
            
            SchedulerUtil.runTask(plugin, setter, () -> 
                plugin.getMessageManager().sendMessage(setter, "bounty.set-success", placeholders));
            
            if (plugin.getConfigManager().getBoolean("bounty.broadcast-enabled", true)) {
                SchedulerUtil.runTask(plugin, () -> {
                    for (Player online : plugin.getServer().getOnlinePlayers()) {
                        if (!online.getUniqueId().equals(setter.getUniqueId())) {
                            plugin.getMessageManager().sendMessage(online, "bounty.broadcast", placeholders);
                        }
                    }
                });
            }
            
            return true;
        });
    }
    
    public CompletableFuture<Boolean> claimBounty(Player hunter, Player target) {
        return CompletableFuture.supplyAsync(() -> {
            BountyEntry bounty = activeBounties.get(target.getUniqueId());
            if (bounty == null) {
                SchedulerUtil.runTask(plugin, hunter, () -> 
                    plugin.getMessageManager().sendMessage(hunter, "bounty.not-found",
                            Map.of("target", target.getName())));
                return false;
            }
            
            double taxPercent = plugin.getConfigManager().getDouble("bounty.tax-percent");
            double taxAmount = bounty.getAmount() * (taxPercent / 100);
            double reward = bounty.getAmount() - taxAmount;
            
            // Pay the hunter
            plugin.getEconomyHook().depositPlayer(hunter, reward);
            
            // Mark bounty as completed
            bounty.setCompleted(true);
            bounty.setHunterUUID(hunter.getUniqueId());
            bounty.setHunterName(hunter.getName());
            bounty.setCompletedAt(System.currentTimeMillis());
            
            // Remove from active bounties
            activeBounties.remove(target.getUniqueId());
            
            // Send messages
            Map<String, String> placeholders = Map.of(
                    "hunter", hunter.getName(),
                    "target", target.getName(),
                    "reward", plugin.getEconomyHook().format(reward),
                    "tax", plugin.getEconomyHook().format(taxAmount),
                    "original", plugin.getEconomyHook().format(bounty.getAmount())
            );
            
            SchedulerUtil.runTask(plugin, hunter, () -> 
                plugin.getMessageManager().sendMessage(hunter, "bounty.claim-success", placeholders));
            
            // Notify the bounty setter if online
            Player setter = plugin.getServer().getPlayer(bounty.getSetterUUID());
            if (setter != null && setter.isOnline()) {
                SchedulerUtil.runTask(plugin, setter, () -> 
                    plugin.getMessageManager().sendMessage(setter, "bounty.claimed-notify", placeholders));
            }
            
            return true;
        });
    }
    
    // ... rest of the methods remain the same as previous version
    public BountyEntry getBounty(UUID targetUUID) {
        return activeBounties.get(targetUUID);
    }
    
    public List<BountyEntry> getActiveBounties() {
        return new ArrayList<>(activeBounties.values());
    }
    
    public boolean removeBounty(Player remover, UUID targetUUID) {
        BountyEntry bounty = activeBounties.get(targetUUID);
        if (bounty == null) {
            return false;
        }
        
        // Refund the setter
        Player setter = plugin.getServer().getPlayer(bounty.getSetterUUID());
        if (setter != null && setter.isOnline()) {
            plugin.getEconomyHook().depositPlayer(setter, bounty.getAmount());
            SchedulerUtil.runTask(plugin, setter, () -> 
                plugin.getMessageManager().sendMessage(setter, "bounty.refunded",
                        Map.of("amount", plugin.getEconomyHook().format(bounty.getAmount()))));
        }
        
        // Remove bounty
        activeBounties.remove(targetUUID);
        return true;
    }
    
    public boolean addHunterMission(Player hunter, UUID targetUUID) {
        Set<UUID> missions = hunterMissions.computeIfAbsent(hunter.getUniqueId(), k -> new HashSet<>());
        
        if (missions.size() >= plugin.getConfigManager().getInt("hunter.max-active", 3)) {
            return false;
        }
        
        return missions.add(targetUUID);
    }
    
    public boolean removeHunterMission(Player hunter, UUID targetUUID) {
        Set<UUID> missions = hunterMissions.get(hunter.getUniqueId());
        return missions != null && missions.remove(targetUUID);
    }
    
    public Set<UUID> getHunterMissions(Player hunter) {
        return hunterMissions.getOrDefault(hunter.getUniqueId(), new HashSet<>());
    }
}
