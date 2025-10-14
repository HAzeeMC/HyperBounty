package com.hazee.hyperbounty.vault;

import com.hazee.hyperbounty.HyperBounty;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.text.DecimalFormat;
import java.util.logging.Level;

public class VaultEconomyHook {
    
    private final HyperBounty plugin;
    private Object economy; // Sử dụng Object thay vì Economy cụ thể
    private final DecimalFormat decimalFormat;
    private boolean vaultEnabled = false;
    
    public VaultEconomyHook(HyperBounty plugin) {
        this.plugin = plugin;
        this.decimalFormat = new DecimalFormat("#,##0.00");
    }
    
    public boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault not found! Using internal economy fallback.");
            return setupFallbackEconomy();
        }
        
        try {
            // Sử dụng reflection để tránh dependency lúc compile
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            RegisteredServiceProvider<?> rsp = Bukkit.getServicesManager().getRegistration(economyClass);
            if (rsp == null) {
                plugin.getLogger().warning("Vault economy provider not found! Using fallback.");
                return setupFallbackEconomy();
            }
            
            economy = rsp.getProvider();
            vaultEnabled = economy != null;
            
            if (vaultEnabled) {
                plugin.getLogger().info("Vault economy hooked successfully: " + economy.getClass().getSimpleName());
            } else {
                plugin.getLogger().warning("Vault economy provider is null! Using fallback.");
                return setupFallbackEconomy();
            }
            
            return true;
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("Vault economy class not found! Using fallback.");
            return setupFallbackEconomy();
        }
    }
    
    private boolean setupFallbackEconomy() {
        plugin.getLogger().warning("Using internal economy fallback. Real economy operations will not work.");
        economy = new FallbackEconomy();
        return true; // Vẫn return true để plugin hoạt động
    }
    
    public boolean has(Player player, double amount) {
        if (!vaultEnabled) {
            return true; // Fallback: luôn return true
        }
        
        try {
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            java.lang.reflect.Method hasMethod = economyClass.getMethod("has", Player.class, double.class);
            return (Boolean) hasMethod.invoke(economy, player, amount);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error checking balance with Vault", e);
            return true;
        }
    }
    
    public double getBalance(Player player) {
        if (!vaultEnabled) {
            return 1000000.0; // Fallback: số dư lớn
        }
        
        try {
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            java.lang.reflect.Method getBalanceMethod = economyClass.getMethod("getBalance", Player.class);
            return (Double) getBalanceMethod.invoke(economy, player);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error getting balance with Vault", e);
            return 1000000.0;
        }
    }
    
    public void withdrawPlayer(Player player, double amount) {
        if (!vaultEnabled) {
            plugin.getLogger().info("FALLBACK: Withdraw " + amount + " from " + player.getName());
            return;
        }
        
        try {
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            java.lang.reflect.Method withdrawMethod = economyClass.getMethod("withdrawPlayer", Player.class, double.class);
            withdrawMethod.invoke(economy, player, amount);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error withdrawing with Vault", e);
        }
    }
    
    public void depositPlayer(Player player, double amount) {
        if (!vaultEnabled) {
            plugin.getLogger().info("FALLBACK: Deposit " + amount + " to " + player.getName());
            return;
        }
        
        try {
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            java.lang.reflect.Method depositMethod = economyClass.getMethod("depositPlayer", Player.class, double.class);
            depositMethod.invoke(economy, player, amount);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error depositing with Vault", e);
        }
    }
    
    public String format(double amount) {
        if (!vaultEnabled) {
            return decimalFormat.format(amount) + " coins";
        }
        
        try {
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            java.lang.reflect.Method formatMethod = economyClass.getMethod("format", double.class);
            return (String) formatMethod.invoke(economy, amount);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error formatting amount with Vault", e);
            return decimalFormat.format(amount) + " coins";
        }
    }
    
    public boolean isVaultEnabled() {
        return vaultEnabled;
    }
    
    // Fallback economy implementation
    private static class FallbackEconomy {
        // Empty class for fallback
    }
}
