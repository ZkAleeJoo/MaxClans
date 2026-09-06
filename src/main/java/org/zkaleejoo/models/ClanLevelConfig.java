package org.zkaleejoo.models;

public class ClanLevelConfig {

    private final int level;
    private final int expRequired;
    private final int maxMembers;
    private final int maxAllies;
    private final int maxHomes;
    private final boolean bankAccess;
    private final boolean chestAccess;
    private final int chestRows;
    private final boolean baseEffect;
    private final String baseEffectType;
    private final int baseEffectAmplifier;
    private final int baseEffectRadius;

    public ClanLevelConfig(int level, int expRequired, int maxMembers, int maxAllies, int maxHomes,
                           boolean bankAccess, boolean chestAccess, int chestRows,
                           boolean baseEffect, String baseEffectType, int baseEffectAmplifier, int baseEffectRadius) {
        this.level = level;
        this.expRequired = Math.max(0, expRequired);
        this.maxMembers = Math.max(1, maxMembers);
        this.maxAllies = Math.max(0, maxAllies);
        this.maxHomes = Math.max(1, maxHomes);
        this.bankAccess = bankAccess;
        this.chestAccess = chestAccess;
        this.chestRows = Math.max(1, Math.min(6, chestRows));
        this.baseEffect = baseEffect;
        this.baseEffectType = baseEffectType != null ? baseEffectType : "HASTE";
        this.baseEffectAmplifier = Math.max(0, baseEffectAmplifier);
        this.baseEffectRadius = Math.max(5, baseEffectRadius);
    }

    public int getLevel() {
        return level;
    }

    public int getExpRequired() {
        return expRequired;
    }

    public int getMaxMembers() {
        return maxMembers;
    }

    public int getMaxAllies() {
        return maxAllies;
    }

    public int getMaxHomes() {
        return maxHomes;
    }

    public boolean hasBankAccess() {
        return bankAccess;
    }

    public boolean hasChestAccess() {
        return chestAccess;
    }

    public int getChestRows() {
        return chestRows;
    }

    public boolean hasBaseEffect() {
        return baseEffect;
    }

    public String getBaseEffectType() {
        return baseEffectType;
    }

    public int getBaseEffectAmplifier() {
        return baseEffectAmplifier;
    }

    public int getBaseEffectRadius() {
        return baseEffectRadius;
    }
}
