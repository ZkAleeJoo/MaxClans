package org.zkaleejoo.models;

import java.util.*;

public class Clan {

    private final String name;
    private String tag;
    private UUID owner;
    private boolean friendlyFire;
    private final long createdAt;
    private final Map<UUID, ClanPlayer> members;
    private final Set<UUID> pendingInvites;

    public Clan(String name, String tag, UUID owner) {
        this.name = name;
        this.tag = tag;
        this.owner = owner;
        this.friendlyFire = false;
        this.createdAt = System.currentTimeMillis();
        this.members = new HashMap<>();
        this.pendingInvites = new HashSet<>();
    }

    public Clan(String name, String tag, UUID owner, boolean friendlyFire, long createdAt) {
        this.name = name;
        this.tag = tag;
        this.owner = owner;
        this.friendlyFire = friendlyFire;
        this.createdAt = createdAt;
        this.members = new HashMap<>();
        this.pendingInvites = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public boolean isFriendlyFire() {
        return friendlyFire;
    }

    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Map<UUID, ClanPlayer> getMembers() {
        return Collections.unmodifiableMap(members);
    }

    public int getMemberCount() {
        return members.size();
    }

    public boolean hasMember(UUID uuid) {
        return members.containsKey(uuid);
    }

    public ClanPlayer getMember(UUID uuid) {
        return members.get(uuid);
    }

    public void addMember(ClanPlayer clanPlayer) {
        members.put(clanPlayer.getUuid(), clanPlayer);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public List<ClanPlayer> getMembersByRole(ClanRole role) {
        List<ClanPlayer> result = new ArrayList<>();
        for (ClanPlayer cp : members.values()) {
            if (cp.getRole() == role) {
                result.add(cp);
            }
        }
        return result;
    }

    public Set<UUID> getPendingInvites() {
        return Collections.unmodifiableSet(pendingInvites);
    }

    public boolean hasInvite(UUID uuid) {
        return pendingInvites.contains(uuid);
    }

    public void addInvite(UUID uuid) {
        pendingInvites.add(uuid);
    }

    public void removeInvite(UUID uuid) {
        pendingInvites.remove(uuid);
    }
}
