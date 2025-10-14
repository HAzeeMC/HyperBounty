package com.hazee.hyperbounty.placeholder;

import com.hazee.hyperbounty.HyperBounty;
import com.hazee.hyperbounty.manager.BountyManager;
import com.hazee.hyperbounty.manager.CooldownManager;
import com.hazee.hyperbounty.manager.KillstreakManager;
import com.hazee.hyperbounty.model.BountyEntry;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlaceholderAPIExpansion {
    
    private final HyperBounty plugin;
    private boolean placeholderApiEnabled = false;
    
    public PlaceholderAPIExpansion(HyperBounty plugin) {
        this.plugin = plugin;
    }
    
    public void register() {
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            plugin.getLogger().info("PlaceholderAPI not found, placeholders disabled.");
            placeholderApiEnabled = false;
            return;
        }
        
        try {
            // Sử dụng reflection để đăng ký expansion
            Class<?> placeholderAPI = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            java.lang.reflect.Method registerMethod = placeholderAPI.getMethod("registerExpansion", Object.class);
            registerMethod.invoke(null, this);
            
            plugin.getLogger().info("PlaceholderAPI expansion registered successfully!");
            placeholderApiEnabled = true;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to register PlaceholderAPI expansion: " + e.getMessage());
            placeholderApiEnabled = false;
        }
    }
    
    public void unregister() {
        if (!placeholderApiEnabled) return;
        
        try {
            Class<?> placeholderAPI = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            java.lang.reflect.Method unregisterMethod = placeholderAPI.getMethod("unregisterExpansion", Object.class);
            unregisterMethod.invoke(null, this);
        } catch (Exception e) {
            plugin.getLogger().warning("Error unregistering PlaceholderAPI expansion: " + e.getMessage());
        }
    }
    
    // Placeholder methods - sẽ được gọi qua reflection bởi PlaceholderAPI
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return null;
        
        String[] args = params.split("_");
        if (args.length == 0) return null;
        
        switch (args[0].toLowerCase()) {
            case "bounty_amount":
                return getBountyAmount(player);
            case "bounty_setter":
                return getBountySetter(player);
            case "has_bounty":
                return hasBounty(player) ? "Yes" : "No";
            case "killstreak":
                return String.valueOf(getKillstreak(player));
            case "active_bounties":
                return String.valueOf(getActiveBountiesCount());
            case "cooldown_kill":
                return formatCooldown(player, "kill");
            case "cooldown_reward":
                return formatCooldown(player, "reward");
            case "cooldown_bounty":
                return formatCooldown(player, "bounty");
            case "hunter_missions":
                return String.valueOf(getHunterMissionsCount(player));
            case "top_bounty_1":
                return getTopBounty(1);
            case "top_bounty_2":
                return getTopBounty(2);
            case "top_bounty_3":
                return getTopBounty(3);
            case "top_bounty_1_amount":
                return getTopBountyAmount(1);
            case "top_bounty_2_amount":
                return getTopBountyAmount(2);
            case "top_bounty_3_amount":
                return getTopBountyAmount(3);
            default:
                return null;
        }
    }
    
    // Các method helper
    private String getBountyAmount(Player player) {
        BountyManager bountyManager = plugin.getBountyManager();
        BountyEntry bounty = bountyManager.getBounty(player.getUniqueId());
        return bounty != null ? plugin.getEconomyHook().format(bounty.getAmount()) : "0";
    }
    
    private String getBountySetter(Player player) {
        BountyManager bountyManager = plugin.getBountyManager();
        BountyEntry bounty = bountyManager.getBounty(player.getUniqueId());
        return bounty != null ? bounty.getSetterName() : "None";
    }
    
    private boolean hasBounty(Player player) {
        BountyManager bountyManager = plugin.getBountyManager();
        return bountyManager.getBounty(player.getUniqueId()) != null;
    }
    
    private int getKillstreak(Player player) {
        KillstreakManager killstreakManager = plugin.getKillstreakManager();
        return killstreakManager.getKillstreak(player);
    }
    
    private int getActiveBountiesCount() {
        BountyManager bountyManager = plugin.getBountyManager();
        return bountyManager.getActiveBounties().size();
    }
    
    private String formatCooldown(Player player, String type) {
        CooldownManager cooldownManager = plugin.getCooldownManager();
        long remaining = 0;
        
        switch (type.toLowerCase()) {
            case "kill":
                remaining = cooldownManager.getKillCooldownRemaining(player) / 1000;
                break;
            case "reward":
                remaining = cooldownManager.getRewardCooldownRemaining(player) / 1000;
                break;
            case "bounty":
                remaining = cooldownManager.getBountyCooldownRemaining(player) / 1000;
                break;
        }
        
        return String.valueOf(remaining);
    }
    
    private int getHunterMissionsCount(Player player) {
        BountyManager bountyManager = plugin.getBountyManager();
        return bountyManager.getHunterMissions(player).size();
    }
    
    private String getTopBounty(int position) {
        BountyManager bountyManager = plugin.getBountyManager();
        List<BountyEntry> bounties = bountyManager.getActiveBounties();
        
        if (position < 1 || position > bounties.size()) {
            return "None";
        }
        
        bounties.sort((b1, b2) -> Double.compare(b2.getAmount(), b1.getAmount()));
        return bounties.get(position - 1).getTargetName();
    }
    
    private String getTopBountyAmount(int position) {
        BountyManager bountyManager = plugin.getBountyManager();
        List<BountyEntry> bounties = bountyManager.getActiveBounties();
        
        if (position < 1 || position > bounties.size()) {
            return "0";
        }
        
        bounties.sort((b1, b2) -> Double.compare(b2.getAmount(), b1.getAmount()));
        return plugin.getEconomyHook().format(bounties.get(position - 1).getAmount());
    }
    
    // Methods để PlaceholderAPI reflection gọi đến
    public String getIdentifier() {
        return "hyperbounty";
    }
    
    public String getAuthor() {
        return "H_Azee";
    }
    
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    public boolean persist() {
        return true;
    }
    
    public String onRequest(OfflinePlayer player, String params) {
        if (player.isOnline()) {
            return onPlaceholderRequest(player.getPlayer(), params);
        }
        return null;
    }
}
