package com.hazee.hyperbounty.manager;

import com.hazee.hyperbounty.HyperBounty;
import com.hazee.hyperbounty.model.BountyEntry;
import org.bukkit.entity.Player;

import java.util.*;
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
        List<BountyEntry> bounties = plugin.getDatabaseManager().getActiveBounties();
        for (BountyEntry bounty : bounties) {
            activeBounties.put(bounty.getTargetUUID(), bounty);
        }
        plugin.getLogger().info("Loaded " + bounties.size() + " active bounties");
    }
    
    public boolean setBounty(Player setter, Player target, double amount) {
        double minAmount = plugin.getConfigManager().getDouble("bounty.min-amount");
        double maxAmount = plugin.getConfigManager().getDouble("bounty.max-amount");
        
        if (amount < minAmount) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("min_amount", String.valueOf(minAmount));
            plugin.getMessageManager().sendMessage(setter, "bounty.amount-too-low", placeholders);
            return false;
        }
        
        if (amount > maxAmount) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("max_amount", String.valueOf(maxAmount));
            plugin.getMessageManager().sendMessage(setter, "bounty.amount-too-high", placeholders);
            return false;
        }
        
        if (!plugin.getEconomyHook().has(setter, amount)) {
            plugin.getMessageManager().sendMessage(setter, "bounty.insufficient-funds");
            return false;
        }
        
        BountyEntry existingBounty = activeBounties.get(target.getUniqueId());
        if (existingBounty != null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("target", target.getName());
            plugin.getMessageManager().sendMessage(setter, "bounty.already-exists", placeholders);
            return false;
        }
        
        BountyEntry bounty = new BountyEntry(
                target.getUniqueId(),
                target.getName(),
                setter.getUniqueId(),
                setter.getName(),
                amount
        );
        
        plugin.getEconomyHook().withdrawPlayer(setter, amount);
        plugin.getDatabaseManager().saveBounty(bounty);
        activeBounties.put(target.getUniqueId(), bounty);
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("setter", setter.getName());
        placeholders.put("target", target.getName());
        placeholders.put("amount", plugin.getEconomyHook().format(amount));
        
        plugin.getMessageManager().sendMessage(setter, "bounty.set-success", placeholders);
        
        if (plugin.getConfigManager().getBoolean("bounty.broadcast-enabled")) {
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                if (!online.getUniqueId().equals(setter.getUniqueId())) {
                    plugin.getMessageManager().sendMessage(online, "bounty.broadcast", placeholders);
                }
            }
        }
        
        return true;
    }
    
    public boolean claimBounty(Player hunter, Player target) {
        BountyEntry bounty = activeBounties.get(target.getUniqueId());
        if (bounty == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("target", target.getName());
            plugin.getMessageManager().sendMessage(hunter, "bounty.not-found", placeholders);
            return false;
        }
        
        double taxPercent = plugin.getConfigManager().getDouble("bounty.tax-percent");
        double taxAmount = bounty.getAmount() * (taxPercent / 100);
        double reward = bounty.getAmount() - taxAmount;
        
        plugin.getEconomyHook().depositPlayer(hunter, reward);
        
        bounty.setCompleted(true);
        bounty.setHunterUUID(hunter.getUniqueId());
        bounty.setHunterName(hunter.getName());
        bounty.setCompletedAt(System.currentTimeMillis());
        
        activeBounties.remove(target.getUniqueId());
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("hunter", hunter.getName());
        placeholders.put("target", target.getName());
        placeholders.put("reward", plugin.getEconomyHook().format(reward));
        placeholders.put("tax", plugin.getEconomyHook().format(taxAmount));
        placeholders.put("original", plugin.getEconomyHook().format(bounty.getAmount()));
        
        plugin.getMessageManager().sendMessage(hunter, "bounty.claim-success", placeholders);
        
        Player setter = plugin.getServer().getPlayer(bounty.getSetterUUID());
        if (setter != null && setter.isOnline()) {
            plugin.getMessageManager().sendMessage(setter, "bounty.claimed-notify", placeholders);
        }
        
        return true;
    }
    
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
        
        Player setter = plugin.getServer().getPlayer(bounty.getSetterUUID());
        if (setter != null && setter.isOnline()) {
            plugin.getEconomyHook().depositPlayer(setter, bounty.getAmount());
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("amount", plugin.getEconomyHook().format(bounty.getAmount()));
            plugin.getMessageManager().sendMessage(setter, "bounty.refunded", placeholders);
        }
        
        activeBounties.remove(targetUUID);
        return true;
    }
    
    public boolean addHunterMission(Player hunter, UUID targetUUID) {
        Set<UUID> missions = hunterMissions.computeIfAbsent(hunter.getUniqueId(), k -> new HashSet<>());
        
        if (missions.size() >= plugin.getConfigManager().getInt("hunter.max-active")) {
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
