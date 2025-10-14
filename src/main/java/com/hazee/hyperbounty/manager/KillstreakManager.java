package com.hazee.hyperbounty.manager;

import com.hazee.hyperbounty.HyperBounty;
import com.hazee.hyperbounty.model.Killstreak;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KillstreakManager {
    
    private final HyperBounty plugin;
    private final Map<UUID, Killstreak> killstreaks;
    private final Map<Integer, Double> killstreakRewards;
    
    public KillstreakManager(HyperBounty plugin) {
        this.plugin = plugin;
        this.killstreaks = new ConcurrentHashMap<>();
        this.killstreakRewards = new HashMap<>();
        loadKillstreakRewards();
    }
    
    private void loadKillstreakRewards() {
        if (!plugin.getConfigManager().getBoolean("killstreaks.enabled")) {
            return;
        }
        
        ConfigurationSection rewardsSection = (ConfigurationSection) plugin.getConfigManager().getSetting("killstreaks.rewards");
        if (rewardsSection != null) {
            for (String key : rewardsSection.getKeys(false)) {
                try {
                    int streak = Integer.parseInt(key);
                    double reward = rewardsSection.getDouble(key);
                    killstreakRewards.put(streak, reward);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid killstreak reward key: " + key);
                }
            }
        }
    }
    
    public void handleKill(Player killer, Player victim) {
        if (!plugin.getConfigManager().getBoolean("killstreaks.enabled")) {
            return;
        }
        
        UUID killerUUID = killer.getUniqueId();
        Killstreak killstreak = killstreaks.computeIfAbsent(killerUUID, 
            k -> new Killstreak(killerUUID, killer.getName()));
        
        long killCooldown = plugin.getConfigManager().getLong("cooldowns.kill");
        
        if (killstreak.isActive(killCooldown)) {
            killstreak.incrementStreak();
        } else {
            killstreak.setStreak(1);
            killstreak.setLastKill(System.currentTimeMillis());
        }
        
        checkKillstreakReward(killer, killstreak.getStreak());
    }
    
    private void checkKillstreakReward(Player player, int streak) {
        Double reward = killstreakRewards.get(streak);
        if (reward != null && reward > 0) {
            plugin.getEconomyHook().depositPlayer(player, reward);
            
            Map<String, String> placeholders = Map.of(
                    "player", player.getName(),
                    "streak", String.valueOf(streak),
                    "reward", plugin.getEconomyHook().format(reward)
            );
            
            plugin.getMessageManager().sendMessage(player, "killstreak.reward", placeholders);
            
            if (plugin.getConfigManager().getBoolean("killstreaks.broadcast", true)) {
                for (Player online : plugin.getServer().getOnlinePlayers()) {
                    if (!online.getUniqueId().equals(player.getUniqueId())) {
                        plugin.getMessageManager().sendMessage(online, "killstreak.broadcast", placeholders);
                    }
                }
            }
        }
    }
    
    public int getKillstreak(Player player) {
        Killstreak killstreak = killstreaks.get(player.getUniqueId());
        return killstreak != null ? killstreak.getStreak() : 0;
    }
    
    public void resetKillstreak(Player player) {
        Killstreak killstreak = killstreaks.get(player.getUniqueId());
        if (killstreak != null) {
            killstreak.resetStreak();
        }
    }
    
    public Map<UUID, Killstreak> getKillstreaks() {
        return new HashMap<>(killstreaks);
    }
}