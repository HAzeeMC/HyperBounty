package com.hazee.hyperbounty.placeholder;

import com.hazee.hyperbounty.HyperBounty;
import com.hazee.hyperbounty.manager.BountyManager;
import com.hazee.hyperbounty.manager.CooldownManager;
import com.hazee.hyperbounty.manager.KillstreakManager;
import com.hazee.hyperbounty.model.BountyEntry;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {
    
    private final HyperBounty plugin;
    
    public PlaceholderAPIExpansion(HyperBounty plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public @NotNull String getIdentifier() {
        return "hyperbounty";
    }
    
    @Override
    public @NotNull String getAuthor() {
        return "H_Azee";
    }
    
    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
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
    
    private String getBountyAmount(OfflinePlayer player) {
        if (!player.isOnline()) return "0";
        
        BountyManager bountyManager = plugin.getBountyManager();
        try {
            BountyEntry bounty = bountyManager.getBounty(player.getUniqueId()).get(2, TimeUnit.SECONDS);
            return bounty != null ? plugin.getEconomyHook().format(bounty.getAmount()) : "0";
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return "0";
        }
    }
    
    private String getBountySetter(OfflinePlayer player) {
        if (!player.isOnline()) return "None";
        
        BountyManager bountyManager = plugin.getBountyManager();
        try {
            BountyEntry bounty = bountyManager.getBounty(player.getUniqueId()).get(2, TimeUnit.SECONDS);
            return bounty != null ? bounty.getSetterName() : "None";
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return "None";
        }
    }
    
    private boolean hasBounty(OfflinePlayer player) {
        if (!player.isOnline()) return false;
        
        BountyManager bountyManager = plugin.getBountyManager();
        try {
            BountyEntry bounty = bountyManager.getBounty(player.getUniqueId()).get(2, TimeUnit.SECONDS);
            return bounty != null;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return false;
        }
    }
    
    private int getKillstreak(OfflinePlayer player) {
        if (!player.isOnline()) return 0;
        
        KillstreakManager killstreakManager = plugin.getKillstreakManager();
        return killstreakManager.getKillstreak(player.getPlayer());
    }
    
    private int getActiveBountiesCount() {
        BountyManager bountyManager = plugin.getBountyManager();
        return bountyManager.getActiveBounties().size();
    }
    
    private String formatCooldown(OfflinePlayer player, String type) {
        if (!player.isOnline()) return "0";
        
        CooldownManager cooldownManager = plugin.getCooldownManager();
        Player onlinePlayer = player.getPlayer();
        long remaining = 0;
        
        switch (type.toLowerCase()) {
            case "kill":
                remaining = cooldownManager.getKillCooldownRemaining(onlinePlayer) / 1000;
                break;
            case "reward":
                remaining = cooldownManager.getRewardCooldownRemaining(onlinePlayer) / 1000;
                break;
            case "bounty":
                remaining = cooldownManager.getBountyCooldownRemaining(onlinePlayer) / 1000;
                break;
        }
        
        return String.valueOf(remaining);
    }
    
    private int getHunterMissionsCount(OfflinePlayer player) {
        if (!player.isOnline()) return 0;
        
        BountyManager bountyManager = plugin.getBountyManager();
        return bountyManager.getHunterMissions(player.getPlayer()).size();
    }
    
    private String getTopBounty(int position) {
        BountyManager bountyManager = plugin.getBountyManager();
        List<BountyEntry> bounties = bountyManager.getActiveBounties();
        
        if (position < 1 || position > bounties.size()) {
            return "None";
        }
        
        // Sort by amount descending
        bounties.sort((b1, b2) -> Double.compare(b2.getAmount(), b1.getAmount()));
        
        return bounties.get(position - 1).getTargetName();
    }
    
    private String getTopBountyAmount(int position) {
        BountyManager bountyManager = plugin.getBountyManager();
        List<BountyEntry> bounties = bountyManager.getActiveBounties();
        
        if (position < 1 || position > bounties.size()) {
            return "0";
        }
        
        // Sort by amount descending
        bounties.sort((b1, b2) -> Double.compare(b2.getAmount(), b1.getAmount()));
        
        return plugin.getEconomyHook().format(bounties.get(position - 1).getAmount());
    }
}
