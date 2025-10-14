package com.hazee.hyperbounty.gui;

import com.hazee.hyperbounty.HyperBounty;
import com.hazee.hyperbounty.manager.BountyManager;
import com.hazee.hyperbounty.manager.CooldownManager;
import com.hazee.hyperbounty.manager.KillstreakManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player; // THÊM IMPORT NÀY
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GUIBuilder {
    
    private final HyperBounty plugin;
    
    public GUIBuilder(HyperBounty plugin) {
        this.plugin = plugin;
    }
    
    public void addNavigationItems(Inventory gui, Player player) {
        // Hunter Missions button
        ItemStack missions = createNavigationItem(
                Material.COMPASS,
                "Hunter Missions",
                Arrays.asList("View your active bounty missions")
        );
        gui.setItem(48, missions);
        
        // Player Stats button
        ItemStack stats = createPlayerStatsItem(player);
        gui.setItem(49, stats);
        
        // Refresh button
        ItemStack refresh = createNavigationItem(
                Material.EMERALD,
                "Refresh Bounties",
                Arrays.asList("Click to refresh the bounty list")
        );
        gui.setItem(50, refresh);
    }
    
    public void addPlayerInfoItem(Inventory gui, Player player) {
        ItemStack playerInfo = createPlayerStatsItem(player);
        gui.setItem(4, playerInfo);
    }
    
    public ItemStack createPlayerStatsItem(Player player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        
        meta.setOwningPlayer(player);
        meta.displayName(Component.text("§6" + player.getName() + "'s Stats"));
        
        BountyManager bountyManager = plugin.getBountyManager();
        CooldownManager cooldownManager = plugin.getCooldownManager();
        KillstreakManager killstreakManager = plugin.getKillstreakManager();
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7Killstreak: §e" + killstreakManager.getKillstreak(player)));
        lore.add(Component.text("§7Active Missions: §a" + bountyManager.getHunterMissions(player).size()));
        
        if (cooldownManager.isOnBountyCooldown(player)) {
            long remaining = cooldownManager.getBountyCooldownRemaining(player) / 1000;
            lore.add(Component.text("§7Bounty Cooldown: §c" + remaining + "s"));
        } else {
            lore.add(Component.text("§7Bounty Cooldown: §aReady"));
        }
        
        if (cooldownManager.isOnRewardCooldown(player)) {
            long remaining = cooldownManager.getRewardCooldownRemaining(player) / 1000;
            lore.add(Component.text("§7Reward Cooldown: §c" + remaining + "s"));
        } else {
            lore.add(Component.text("§7Reward Cooldown: §aReady"));
        }
        
        lore.add(Component.text(""));
        lore.add(Component.text("§eBalance: §6" + plugin.getEconomyHook().format(plugin.getEconomyHook().getBalance(player))));
        
        meta.lore(lore);
        skull.setItemMeta(meta);
        
        return skull;
    }
    
    public ItemStack createNavigationItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("§b" + name));
        
        List<Component> componentLore = new ArrayList<>();
        for (String line : lore) {
            componentLore.add(Component.text("§7" + line));
        }
        
        meta.lore(componentLore);
        item.setItemMeta(meta);
        
        return item;
    }
}
