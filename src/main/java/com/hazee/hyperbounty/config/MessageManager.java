package com.hazee.hyperbounty.config;

import com.hazee.hyperbounty.HyperBounty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {
    
    private final HyperBounty plugin;
    private final MiniMessage miniMessage;
    private FileConfiguration messages;
    private String currentLanguage;
    private Map<String, String> messageCache;
    
    public MessageManager(HyperBounty plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.messageCache = new HashMap<>();
    }
    
    public void loadMessages() {
        String language = plugin.getConfigManager().getString("settings.language", "en-english");
        currentLanguage = language;
        
        File messagesFile = new File(plugin.getDataFolder(), "messages_" + language + ".yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages_" + language + ".yml", false);
        }
        
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        loadMessageCache();
    }
    
    private void loadMessageCache() {
        messageCache.clear();
        for (String key : messages.getKeys(true)) {
            if (messages.isString(key)) {
                messageCache.put(key, messages.getString(key));
            }
        }
    }
    
    public Component getMessage(String path) {
        String message = messageCache.getOrDefault(path, "Message not found: " + path);
        return miniMessage.deserialize(message);
    }
    
    public Component getMessage(String path, Map<String, String> placeholders) {
        String message = messageCache.getOrDefault(path, "Message not found: " + path);
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        return miniMessage.deserialize(message);
    }
    
    public void sendMessage(Player player, String path) {
        player.sendMessage(getMessage(path));
    }
    
    public void sendMessage(Player player, String path, Map<String, String> placeholders) {
        player.sendMessage(getMessage(path, placeholders));
    }
    
    public String getRawMessage(String path) {
        return messageCache.getOrDefault(path, path);
    }
}