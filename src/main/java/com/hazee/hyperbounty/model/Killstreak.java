package com.hazee.hyperbounty.model;

import java.util.UUID;

public class Killstreak {
    
    private final UUID playerUUID;
    private final String playerName;
    private int streak;
    private long lastKill;
    
    public Killstreak(UUID playerUUID, String playerName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.streak = 0;
        this.lastKill = 0;
    }
    
    public Killstreak(UUID playerUUID, String playerName, int streak, long lastKill) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.streak = streak;
        this.lastKill = lastKill;
    }
    
    // Getters
    public UUID getPlayerUUID() { return playerUUID; }
    public String getPlayerName() { return playerName; }
    public int getStreak() { return streak; }
    public long getLastKill() { return lastKill; }
    
    // Setters
    public void setStreak(int streak) { this.streak = streak; }
    public void setLastKill(long lastKill) { this.lastKill = lastKill; }
    
    public void incrementStreak() {
        this.streak++;
        this.lastKill = System.currentTimeMillis();
    }
    
    public void resetStreak() {
        this.streak = 0;
        this.lastKill = 0;
    }
    
    public boolean isActive(long cooldown) {
        return System.currentTimeMillis() - lastKill <= cooldown;
    }
}