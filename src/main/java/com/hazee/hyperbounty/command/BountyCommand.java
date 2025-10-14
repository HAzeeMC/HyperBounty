package com.hazee.hyperbounty.command;

import com.hazee.hyperbounty.HyperBounty;
import com.hazee.hyperbounty.gui.BountyGUI;
import com.hazee.hyperbounty.manager.BountyManager;
import com.hazee.hyperbounty.manager.CooldownManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class BountyCommand implements CommandExecutor {
    
    private final HyperBounty plugin;
    private final BountyGUI bountyGUI;
    
    public BountyCommand(HyperBounty plugin) {
        this.plugin = plugin;
        this.bountyGUI = new BountyGUI(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be executed by players.");
            return true;
        }
        
        if (args.length == 0) {
            bountyGUI.openMainMenu(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "set":
                handleSetBounty(player, args);
                break;
            case "remove":
                handleRemoveBounty(player, args);
                break;
            case "check":
                handleCheckBounty(player, args);
                break;
            case "list":
                handleListBounties(player);
                break;
            case "reload":
                handleReload(player);
                break;
            case "help":
                sendHelp(player);
                break;
            default:
                plugin.getMessageManager().sendMessage(player, "command.unknown");
                break;
        }
        
        return true;
    }
    
    private void handleSetBounty(Player player, String[] args) {
        if (args.length < 3) {
            plugin.getMessageManager().sendMessage(player, "command.usage-set");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            plugin.getMessageManager().sendMessage(player, "player.not-found");
            return;
        }
        
        if (target.getUniqueId().equals(player.getUniqueId())) {
            plugin.getMessageManager().sendMessage(player, "bounty.cannot-self");
            return;
        }
        
        CooldownManager cooldownManager = plugin.getCooldownManager();
        if (cooldownManager.isOnBountyCooldown(player)) {
            long remaining = cooldownManager.getBountyCooldownRemaining(player) / 1000;
            Map<String, String> placeholders = Map.of("cooldown", String.valueOf(remaining));
            plugin.getMessageManager().sendMessage(player, "cooldown.bounty", placeholders);
            return;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount <= 0) {
                plugin.getMessageManager().sendMessage(player, "bounty.invalid-amount");
                return;
            }
        } catch (NumberFormatException e) {
            plugin.getMessageManager().sendMessage(player, "bounty.invalid-amount");
            return;
        }
        
        plugin.getBountyManager().setBounty(player, target, amount).thenAccept(success -> {
            if (success) {
                cooldownManager.setBountyCooldown(player);
            }
        });
    }
    
    private void handleRemoveBounty(Player player, String[] args) {
        if (!player.hasPermission("hyperbounty.admin")) {
            plugin.getMessageManager().sendMessage(player, "command.no-permission");
            return;
        }
        
        if (args.length < 2) {
            plugin.getMessageManager().sendMessage(player, "command.usage-remove");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            plugin.getMessageManager().sendMessage(player, "player.not-found");
            return;
        }
        
        BountyManager bountyManager = plugin.getBountyManager();
        boolean removed = bountyManager.removeBounty(player, target.getUniqueId());
        
        if (removed) {
            Map<String, String> placeholders = Map.of("target", target.getName());
            plugin.getMessageManager().sendMessage(player, "bounty.removed", placeholders);
        } else {
            plugin.getMessageManager().sendMessage(player, "bounty.not-found");
        }
    }
    
    private void handleCheckBounty(Player player, String[] args) {
        Player target;
        if (args.length > 1) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                plugin.getMessageManager().sendMessage(player, "player.not-found");
                return;
            }
        } else {
            target = player;
        }
        
        plugin.getBountyManager().getBounty(target.getUniqueId()).thenAccept(bounty -> {
            if (bounty != null) {
                Map<String, String> placeholders = Map.of(
                        "target", target.getName(),
                        "amount", plugin.getEconomyHook().format(bounty.getAmount()),
                        "setter", bounty.getSetterName()
                );
                plugin.getMessageManager().sendMessage(player, "bounty.info", placeholders);
            } else {
                Map<String, String> placeholders = Map.of("target", target.getName());
                plugin.getMessageManager().sendMessage(player, "bounty.no-bounty", placeholders);
            }
        });
    }
    
    private void handleListBounties(Player player) {
        bountyGUI.openBountyList(player, 1);
    }
    
    private void handleReload(Player player) {
        if (!player.hasPermission("hyperbounty.admin")) {
            plugin.getMessageManager().sendMessage(player, "command.no-permission");
            return;
        }
        
        plugin.getConfigManager().reloadConfig();
        plugin.getMessageManager().loadMessages();
        plugin.getMessageManager().sendMessage(player, "command.reloaded");
    }
    
    private void sendHelp(Player player) {
        for (int i = 1; i <= 10; i++) {
            String path = "command.help-line-" + i;
            String message = plugin.getMessageManager().getRawMessage(path);
            if (!message.equals(path)) {
                player.sendMessage(plugin.getMessageManager().getMessage(path));
            }
        }
    }
}