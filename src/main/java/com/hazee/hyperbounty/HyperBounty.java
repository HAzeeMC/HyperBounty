package com.hazee.hyperbounty;

import com.hazee.hyperbounty.command.BountyCommand;
import com.hazee.hyperbounty.command.BountyTabCompleter;
import com.hazee.hyperbounty.config.ConfigManager;
import com.hazee.hyperbounty.config.MessageManager;
import com.hazee.hyperbounty.database.DatabaseManager;
import com.hazee.hyperbounty.listener.GUIListener;
import com.hazee.hyperbounty.listener.PlayerListener;
import com.hazee.hyperbounty.manager.BountyManager;
import com.hazee.hyperbounty.manager.CooldownManager;
import com.hazee.hyperbounty.manager.KillstreakManager;
import com.hazee.hyperbounty.placeholder.PlaceholderAPIExpansion;
import com.hazee.hyperbounty.utils.SchedulerUtil;
import com.hazee.hyperbounty.vault.VaultEconomyHook;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class HyperBounty extends JavaPlugin {
    
    private static HyperBounty instance;
    private Logger logger;
    
    private ConfigManager configManager;
    private MessageManager messageManager;
    private DatabaseManager databaseManager;
    private VaultEconomyHook economyHook;
    private PlaceholderAPIExpansion placeholderExpansion;
    
    private BountyManager bountyManager;
    private CooldownManager cooldownManager;
    private KillstreakManager killstreakManager;
    
    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        
        if (SchedulerUtil.isFolia()) {
            logger.info("Folia detected! Using Folia schedulers.");
        } else {
            logger.info("Using Bukkit schedulers.");
        }
        
        initializeManagers();
        registerCommands();
        registerListeners();
        setupPlaceholderAPI();
        startAutoSave();
        
        logger.info("HyperBounty enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        if (placeholderExpansion != null) {
            placeholderExpansion.unregister();
        }
        
        if (databaseManager != null) {
            databaseManager.close();
        }
        logger.info("HyperBounty disabled!");
    }
    
    private void initializeManagers() {
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        databaseManager = new DatabaseManager(this);
        economyHook = new VaultEconomyHook(this);

        configManager.loadConfig();
        messageManager.loadMessages();
        databaseManager.initialize();
        
        bountyManager = new BountyManager(this);
        cooldownManager = new CooldownManager(this);
        killstreakManager = new KillstreakManager(this);
        
        configManager.loadConfig();
        messageManager.loadMessages();
        databaseManager.initialize();
        
        if (!economyHook.setupEconomy()) {
            logger.warning("Vault economy not found! Using fallback economy system.");
        }
    }
    
    private void registerCommands() {
        getCommand("bounty").setExecutor(new BountyCommand(this));
        getCommand("bounty").setTabCompleter(new BountyTabCompleter(this));
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
    }
    
    private void setupPlaceholderAPI() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderExpansion = new PlaceholderAPIExpansion(this);
            placeholderExpansion.register();
            logger.info("PlaceholderAPI expansion registered successfully!");
        } else {
            logger.info("PlaceholderAPI not found, placeholders will not be available.");
        }
    }
    
    private void startAutoSave() {
        int autoSaveInterval = configManager.getInt("settings.auto-save") * 20;
        if (autoSaveInterval > 0) {
            SchedulerUtil.runTaskTimer(this, this::saveData, autoSaveInterval, autoSaveInterval);
        }
    }
    
    private void saveData() {
        logger.fine("Auto-save completed.");
    }
    
    public static HyperBounty getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public VaultEconomyHook getEconomyHook() {
        return economyHook;
    }
    
    public BountyManager getBountyManager() {
        return bountyManager;
    }
    
    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }
    
    public KillstreakManager getKillstreakManager() {
        return killstreakManager;
    }
    
    public PlaceholderAPIExpansion getPlaceholderExpansion() {
        return placeholderExpansion;
    }
}
