package org.zkaleejoo.models;

import java.util.UUID;

public class ClanPlayer {

    private final UUID uuid;
    private String clanName;
    private ClanRole role;

    public ClanPlayer(UUID uuid, String clanName, ClanRole role) {
        this.uuid = uuid;
        this.clanName = clanName;
        this.role = role;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getClanName() {
        return clanName;
    }

    public void setClanName(String clanName) {
        this.clanName = clanName;
    }

    public ClanRole getRole() {
        return role;
    }

    public void setRole(ClanRole role) {
        this.role = role;
    }

    public boolean isLeader() {
        return role == ClanRole.LEADER;
    }

    public boolean isModerator() {
        return role == ClanRole.MODERATOR;
    }

    public boolean hasRoleAtLeast(ClanRole requiredRole) {
        return role.isAtLeast(requiredRole);
    }
}
