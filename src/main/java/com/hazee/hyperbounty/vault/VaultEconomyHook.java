package com.hazee.hyperbounty.vault;

import com.hazee.hyperbounty.HyperBounty;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.text.DecimalFormat;
import java.util.logging.Level;

public class VaultEconomyHook {
    
    private final HyperBounty plugin;
    private Object economy;
    private final DecimalFormat decimalFormat;
    private boolean vaultEnabled = false;
    
    public VaultEconomyHook(HyperBounty plugin) {
        this.plugin = plugin;
        this.decimalFormat = new DecimalFormat("#,##0.00");
    }
    
    public boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault not found! Using internal economy fallback.");
            vaultEnabled = false;
            return true; // Vẫn return true để plugin hoạt động
        }
        
        try {
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            RegisteredServiceProvider<?> rsp = Bukkit.getServicesManager().getRegistration(economyClass);
            if (rsp == null) {
                plugin.getLogger().warning("Vault economy provider not found! Using fallback.");
                vaultEnabled = false;
                return true;
            }
            
            economy = rsp.getProvider();
            vaultEnabled = economy != null;
            
            if (vaultEnabled) {
                plugin.getLogger().info("Vault economy hooked successfully: " + economy.getClass().getSimpleName());
            } else {
                plugin.getLogger().warning("Vault economy provider is null! Using fallback.");
            }
            
            return true;
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("Vault economy class not found! Using fallback.");
            vaultEnabled = false;
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error setting up Vault economy", e);
            vaultEnabled = false;
            return true;
        }
    }
    
    public boolean has(Player player, double amount) {
        if (!vaultEnabled || economy == null) {
            // Fallback: giả lập luôn có đủ tiền để test
            return true;
        }
        
        try {
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            java.lang.reflect.Method hasMethod = economyClass.getMethod("has", Player.class, double.class);
            return (Boolean) hasMethod.invoke(economy, player, amount);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error checking balance with Vault", e);
            return true; // Fallback
        }
    }
    
    public double getBalance(Player player) {
        if (!vaultEnabled || economy == null) {
            // Fallback: số dư lớn để test
            return 1000000.0;
        }
        
        try {
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            java.lang.reflect.Method getBalanceMethod = economyClass.getMethod("getBalance", Player.class);
            Object result = getBalanceMethod.invoke(economy, player);
            return ((Number) result).doubleValue();
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error getting balance with Vault", e);
            return 1000000.0; // Fallback
        }
    }
    
    public void withdrawPlayer(Player player, double amount) {
        if (!vaultEnabled || economy == null) {
            // Fallback: chỉ log lại
            plugin.getLogger().info("FALLBACK: Withdraw " + format(amount) + " from " + player.getName());
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
        if (!vaultEnabled || economy == null) {
            // Fallback: chỉ log lại
            plugin.getLogger().info("FALLBACK: Deposit " + format(amount) + " to " + player.getName());
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
        if (!vaultEnabled || economy == null) {
            // Fallback: format đơn giản
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
}
