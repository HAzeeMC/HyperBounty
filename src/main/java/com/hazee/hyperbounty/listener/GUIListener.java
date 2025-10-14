package com.hazee.hyperbounty.listener;

import com.hazee.hyperbounty.HyperBounty;
import com.hazee.hyperbounty.gui.BountyGUI;
import com.hazee.hyperbounty.manager.BountyManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class GUIListener implements Listener {
    
    private final HyperBounty plugin;
    private final BountyGUI bountyGUI;
    
    public GUIListener(HyperBounty plugin) {
        this.plugin = plugin;
        this.bountyGUI = new BountyGUI(plugin);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        Inventory inventory = event.getInventory();
        String title = event.getView().getTitle();
        
        if (!title.contains("Bounty Hunter")) {
            return;
        }
        
        event.setCancelled(true);
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) {
            return;
        }
        
        handleGUIClick(player, clickedItem, title);
    }
    
    private void handleGUIClick(Player player, ItemStack item, String title) {
        Material type = item.getType();
        
        if (type == Material.PLAYER_HEAD && item.getItemMeta() instanceof SkullMeta) {
            handleBountyClaim(player, item);
        } else if (type == Material.ARROW) {
            handlePagination(player, item, title);
        } else if (type == Material.COMPASS) {
            bountyGUI.openHunterMissions(player);
        } else if (type == Material.EMERALD) {
            bountyGUI.openMainMenu(player);
        }
    }
    
    private void handleBountyClaim(Player player, ItemStack bountyItem) {
        SkullMeta meta = (SkullMeta) bountyItem.getItemMeta();
        if (meta.getOwningPlayer() == null) return;
        
        Player target = meta.getOwningPlayer().getPlayer();
        if (target == null || !target.isOnline()) {
            plugin.getMessageManager().sendMessage(player, "player.offline");
            return;
        }
        
        BountyManager bountyManager = plugin.getBountyManager();
        bountyManager.addHunterMission(player, target.getUniqueId());
        
        plugin.getMessageManager().sendMessage(player, "hunter.mission-added",
                Map.of("target", target.getName()));
    }
    
    private void handlePagination(Player player, ItemStack arrow, String title) {
        int currentPage = bountyGUI.getPlayerPage(player.getUniqueId());
        
        if (arrow.getItemMeta().getDisplayName().contains("Previous")) {
            bountyGUI.openBountyList(player, currentPage - 1);
        } else if (arrow.getItemMeta().getDisplayName().contains("Next")) {
            bountyGUI.openBountyList(player, currentPage + 1);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Clean up if needed
    }
}