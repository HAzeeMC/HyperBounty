package com.hazee.hyperbounty.vault;

import com.hazee.hyperbounty.HyperBounty;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.text.DecimalFormat;

public class VaultEconomyHook {
    
    private final HyperBounty plugin;
    private Economy economy;
    private final DecimalFormat decimalFormat;
    
    public VaultEconomyHook(HyperBounty plugin) {
        this.plugin = plugin;
        this.decimalFormat = new DecimalFormat("#,##0.00");
    }
    
    public boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        
        economy = rsp.getProvider();
        return economy != null;
    }
    
    public boolean has(Player player, double amount) {
        return economy.has(player, amount);
    }
    
    public double getBalance(Player player) {
        return economy.getBalance(player);
    }
    
    public void withdrawPlayer(Player player, double amount) {
        economy.withdrawPlayer(player, amount);
    }
    
    public void depositPlayer(Player player, double amount) {
        economy.depositPlayer(player, amount);
    }
    
    public String format(double amount) {
        return economy.format(amount);
    }
    
    public Economy getEconomy() {
        return economy;
    }
}