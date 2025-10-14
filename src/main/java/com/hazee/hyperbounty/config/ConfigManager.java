package com.hazee.hyperbounty.config;

import com.hazee.hyperbounty.HyperBounty;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    
    private final HyperBounty plugin;
    private FileConfiguration config;
    private File configFile;
    
    private Map<String, Object> settings;
    
    public ConfigManager(HyperBounty plugin) {
        this.plugin = plugin;
        this.settings = new HashMap<>();
    }
    
    public void loadConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        loadSettings();
    }
    
    private void loadSettings() {
        settings.clear();
        
        // Database settings
        settings.put("database.type", config.getString("database.type", "SQLITE"));
        settings.put("database.host", config.getString("database.host", "localhost"));
        settings.put("database.port", config.getInt("database.port", 3306));
        settings.put("database.name", config.getString("database.name", "hyperbounty"));
        settings.put("database.username", config.getString("database.username", "username"));
        settings.put("database.password", config.getString("database.password", "password"));
        
        // Cooldown settings
        settings.put("cooldowns.kill", config.getLong("cooldowns.kill", 30000L));
        settings.put("cooldowns.reward", config.getLong("cooldowns.reward", 60000L));
        settings.put("cooldowns.bounty", config.getLong("cooldowns.bounty", 120000L));
        
        // Bounty settings
        settings.put("bounty.min-amount", config.getDouble("bounty.min-amount", 100.0));
        settings.put("bounty.max-amount", config.getDouble("bounty.max-amount", 10000.0));
        settings.put("bounty.tax-percent", config.getDouble("bounty.tax-percent", 5.0));
        settings.put("bounty.auto-remove", config.getBoolean("bounty.auto-remove", true));
        
        // Killstreak settings
        settings.put("killstreaks.enabled", config.getBoolean("killstreaks.enabled", true));
        settings.put("killstreaks.rewards", config.getConfigurationSection("killstreaks.rewards"));
        
        // GUI settings
        settings.put("gui.title", config.getString("gui.title", "Bounty Hunter"));
        settings.put("gui.size", config.getInt("gui.size", 54));
        settings.put("gui.items", config.getConfigurationSection("gui.items"));
        
        // Hunter mission settings
        settings.put("hunter.enabled", config.getBoolean("hunter.enabled", true));
        settings.put("hunter.reward-multiplier", config.getDouble("hunter.reward-multiplier", 1.5));
        settings.put("hunter.max-active", config.getInt("hunter.max-active", 3));
    }
    
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        loadSettings();
    }
    
    public Object getSetting(String path) {
        return settings.get(path);
    }
    
    // FIX: Thêm overload methods với default values
    public String getString(String path) {
        return config.getString(path);
    }
    
    public String getString(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }
    
    public int getInt(String path) {
        return config.getInt(path);
    }
    
    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }
    
    public double getDouble(String path) {
        return config.getDouble(path);
    }
    
    public double getDouble(String path, double defaultValue) {
        return config.getDouble(path, defaultValue);
    }
    
    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }
    
    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }
    
    public long getLong(String path) {
        return config.getLong(path);
    }
    
    public long getLong(String path, long defaultValue) {
        return config.getLong(path, defaultValue);
    }
    
    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }
}
