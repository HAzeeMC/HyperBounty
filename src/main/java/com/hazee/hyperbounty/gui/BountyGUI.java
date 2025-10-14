package com.hazee.hyperbounty.gui;

import com.hazee.hyperbounty.HyperBounty;
import com.hazee.hyperbounty.model.BountyEntry;
import com.hazee.hyperbounty.utils.SchedulerUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.stream.Collectors;

public class BountyGUI {
    
    private final HyperBounty plugin;
    private final GUIBuilder guiBuilder;
    private final Map<UUID, Integer> playerPages;
    
    public BountyGUI(HyperBounty plugin) {
        this.plugin = plugin;
        this.guiBuilder = new GUIBuilder(plugin);
        this.playerPages = new HashMap<>();
    }
    
    public void openMainMenu(Player player) {
        SchedulerUtil.runTask(plugin, player, () -> {
            String title = plugin.getConfigManager().getString("gui.title", "Bounty Hunter");
            int size = plugin.getConfigManager().getInt("gui.size", 54);
            
            Inventory gui = Bukkit.createInventory(player, size, Component.text(title));
            
            // Add bounty list items
            List<BountyEntry> bounties = plugin.getBountyManager().getActiveBounties();
            int slot = 10;
            
            for (BountyEntry bounty : bounties.stream()
                    .sorted(Comparator.comparingDouble(BountyEntry::getAmount).reversed())
                    .limit(21)
                    .collect(Collectors.toList())) {
                
                if (slot >= 43) break;
                
                ItemStack bountyItem = createBountyItem(bounty);
                gui.setItem(slot, bountyItem);
                
                slot++;
                if ((slot - 9) % 9 == 0) {
                    slot += 2;
                }
            }
            
            // Add navigation and info items
            guiBuilder.addNavigationItems(gui, player);
            guiBuilder.addPlayerInfoItem(gui, player);
            
            player.openInventory(gui);
        });
    }
    
    public void openBountyList(Player player, int page) {
        SchedulerUtil.runTask(plugin, player, () -> {
            List<BountyEntry> allBounties = plugin.getBountyManager().getActiveBounties();
            int itemsPerPage = 45;
            int totalPages = (int) Math.ceil((double) allBounties.size() / itemsPerPage);
            
            if (page < 1) page = 1;
            if (page > totalPages) page = totalPages;
            
            playerPages.put(player.getUniqueId(), page);
            
            String title = plugin.getConfigManager().getString("gui.title", "Bounty Hunter") + " - Page " + page;
            Inventory gui = Bukkit.createInventory(player, 54, Component.text(title));
            
            int startIndex = (page - 1) * itemsPerPage;
            int endIndex = Math.min(startIndex + itemsPerPage, allBounties.size());
            
            for (int i = startIndex; i < endIndex; i++) {
                BountyEntry bounty = allBounties.get(i);
                ItemStack bountyItem = createBountyItem(bounty);
                gui.setItem(i - startIndex, bountyItem);
            }
            
            // Add pagination items
            if (page > 1) {
                ItemStack previousPage = guiBuilder.createNavigationItem(
                        Material.ARROW, 
                        "Previous Page", 
                        Arrays.asList("Click to go to page " + (page - 1))
                );
                gui.setItem(45, previousPage);
            }
            
            if (page < totalPages) {
                ItemStack nextPage = guiBuilder.createNavigationItem(
                        Material.ARROW, 
                        "Next Page", 
                        Arrays.asList("Click to go to page " + (page + 1))
                );
                gui.setItem(53, nextPage);
            }
            
            // Add page info
            ItemStack pageInfo = guiBuilder.createNavigationItem(
                    Material.PAPER,
                    "Page " + page + "/" + totalPages,
                    Arrays.asList("Total bounties: " + allBounties.size())
            );
            gui.setItem(49, pageInfo);
            
            player.openInventory(gui);
        });
    }
    
    private ItemStack createBountyItem(BountyEntry bounty) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        
        // Try to set skull owner
        try {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(bounty.getTargetUUID()));
        } catch (Exception e) {
            // Fallback if skull cannot be set
        }
        
        meta.displayName(Component.text("§6" + bounty.getTargetName() + "'s Bounty"));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7Amount: §e" + plugin.getEconomyHook().format(bounty.getAmount())));
        lore.add(Component.text("§7Setter: §a" + bounty.getSetterName()));
        lore.add(Component.text(""));
        lore.add(Component.text("§eClick to claim bounty!"));
        
        meta.lore(lore);
        skull.setItemMeta(meta);
        
        return skull;
    }
    
    public void openHunterMissions(Player player) {
        SchedulerUtil.runTask(plugin, player, () -> {
            String title = "Hunter Missions";
            Inventory gui = Bukkit.createInventory(player, 54, Component.text(title));
            
            Set<UUID> missions = plugin.getBountyManager().getHunterMissions(player);
            int slot = 10;
            
            for (UUID targetUUID : missions) {
                if (slot >= 43) break;
                
                // FIX: Sử dụng method sync thay vì thenAccept
                BountyEntry bounty = plugin.getBountyManager().getBounty(targetUUID);
                if (bounty != null) {
                    ItemStack missionItem = createMissionItem(bounty);
                    gui.setItem(slot, missionItem);
                }
                
                slot++;
                if ((slot - 9) % 9 == 0) {
                    slot += 2;
                }
            }
            
            player.openInventory(gui);
        });
    }
    
    private ItemStack createMissionItem(BountyEntry bounty) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        
        meta.displayName(Component.text("§bMission: " + bounty.getTargetName()));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7Bounty: §e" + plugin.getEconomyHook().format(bounty.getAmount())));
        lore.add(Component.text("§7Target: §c" + bounty.getTargetName()));
        lore.add(Component.text(""));
        lore.add(Component.text("§aActive Mission"));
        lore.add(Component.text("§7Kill the target to claim reward!"));
        
        meta.lore(lore);
        compass.setItemMeta(meta);
        
        return compass;
    }
    
    public int getPlayerPage(UUID playerUUID) {
        return playerPages.getOrDefault(playerUUID, 1);
    }
}
