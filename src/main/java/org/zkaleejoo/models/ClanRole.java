package org.zkaleejoo.models;

public enum ClanRole {

    LEADER("Leader", 3),
    MODERATOR("Moderator", 2),
    MEMBER("Member", 1);

    private final String displayName;
    private final int weight;

    ClanRole(String displayName, int weight) {
        this.displayName = displayName;
        this.weight = weight;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isAtLeast(ClanRole other) {
        return this.weight >= other.weight;
    }
}
