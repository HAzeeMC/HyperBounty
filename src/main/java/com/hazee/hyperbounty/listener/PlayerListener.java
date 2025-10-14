package com.hazee.hyperbounty.listener;

import com.hazee.hyperbounty.HyperBounty;
import com.hazee.hyperbounty.manager.BountyManager;
import com.hazee.hyperbounty.manager.CooldownManager;
import com.hazee.hyperbounty.manager.KillstreakManager;
import com.hazee.hyperbounty.utils.SchedulerUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;

public class PlayerListener implements Listener {
    
    private final HyperBounty plugin;
    
    public PlayerListener(HyperBounty plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        if (killer == null || killer.equals(victim)) {
            return;
        }
        
        // Use Folia-compatible scheduling
        SchedulerUtil.runTask(plugin, () -> {
            handleBountyClaim(killer, victim);
            handleKillstreak(killer, victim);
        });
    }
    
    private void handleBountyClaim(Player killer, Player victim) {
        BountyManager bountyManager = plugin.getBountyManager();
        CooldownManager cooldownManager = plugin.getCooldownManager();
        
        if (cooldownManager.isOnRewardCooldown(killer)) {
            long remaining = cooldownManager.getRewardCooldownRemaining(killer) / 1000;
            Map<String, String> placeholders = Map.of("cooldown", String.valueOf(remaining));
            plugin.getMessageManager().sendMessage(killer, "cooldown.reward", placeholders);
            return;
        }
        
        bountyManager.getBounty(victim.getUniqueId()).thenAccept(bounty -> {
            if (bounty != null) {
                bountyManager.claimBounty(killer, victim).thenAccept(success -> {
                    if (success) {
                        cooldownManager.setRewardCooldown(killer);
                    }
                });
            }
        });
    }
    
    private void handleKillstreak(Player killer, Player victim) {
        KillstreakManager killstreakManager = plugin.getKillstreakManager();
        CooldownManager cooldownManager = plugin.getCooldownManager();
        
        if (cooldownManager.isOnKillCooldown(killer)) {
            return;
        }
        
        killstreakManager.handleKill(killer, victim);
        cooldownManager.setKillCooldown(killer);
        
        // Reset victim's killstreak
        killstreakManager.resetKillstreak(victim);
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Load player data if needed
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        CooldownManager cooldownManager = plugin.getCooldownManager();
        cooldownManager.clearCooldowns(player.getUniqueId());
    }
}
