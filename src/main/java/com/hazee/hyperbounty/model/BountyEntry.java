package com.hazee.hyperbounty.model;

import java.util.UUID;

public class BountyEntry {
    
    private final UUID targetUUID;
    private final String targetName;
    private final UUID setterUUID;
    private final String setterName;
    private final double amount;
    private UUID hunterUUID;
    private String hunterName;
    private boolean completed;
    private long completedAt;
    
    public BountyEntry(UUID targetUUID, String targetName, UUID setterUUID, String setterName, double amount) {
        this.targetUUID = targetUUID;
        this.targetName = targetName;
        this.setterUUID = setterUUID;
        this.setterName = setterName;
        this.amount = amount;
        this.completed = false;
    }
    
    // Getters
    public UUID getTargetUUID() { return targetUUID; }
    public String getTargetName() { return targetName; }
    public UUID getSetterUUID() { return setterUUID; }
    public String getSetterName() { return setterName; }
    public double getAmount() { return amount; }
    public UUID getHunterUUID() { return hunterUUID; }
    public String getHunterName() { return hunterName; }
    public boolean isCompleted() { return completed; }
    public long getCompletedAt() { return completedAt; }
    
    // Setters
    public void setHunterUUID(UUID hunterUUID) { this.hunterUUID = hunterUUID; }
    public void setHunterName(String hunterName) { this.hunterName = hunterName; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BountyEntry that = (BountyEntry) obj;
        return targetUUID.equals(that.targetUUID);
    }
    
    @Override
    public int hashCode() {
        return targetUUID.hashCode();
    }
}
