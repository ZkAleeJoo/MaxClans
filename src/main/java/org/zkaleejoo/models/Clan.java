package org.zkaleejoo.models;

import java.util.*;

public class Clan {

    private final String name;
    private String tag;
    private UUID owner;
    private final long createdAt;
    private final Map<UUID, ClanPlayer> members;
    private final Set<UUID> pendingInvites;
    private final Set<UUID> joinRequests;
    private final Set<String> allies;
    private final Map<ClanFlag, Boolean> flags;
    private int kills;
    private int deaths;
    private int rivalKills;

    public Clan(String name, String tag, UUID owner) {
        this(name, tag, owner, false, System.currentTimeMillis(), 0, 0, 0);
    }

    public Clan(String name, String tag, UUID owner, boolean friendlyFire, long createdAt) {
        this(name, tag, owner, friendlyFire, createdAt, 0, 0, 0);
    }

    public Clan(String name, String tag, UUID owner, boolean friendlyFire, long createdAt, int kills, int deaths, int rivalKills) {
        this.name = name;
        this.tag = tag;
        this.owner = owner;
        this.createdAt = createdAt;
        this.kills = kills;
        this.deaths = deaths;
        this.rivalKills = rivalKills;
        this.members = new HashMap<>();
        this.pendingInvites = new HashSet<>();
        this.joinRequests = new HashSet<>();
        this.allies = new HashSet<>();
        this.flags = new EnumMap<>(ClanFlag.class);
        initDefaultFlags();
        setFlag(ClanFlag.FRIENDLY_FIRE, friendlyFire);
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

    private void initDefaultFlags() {
        for (ClanFlag flag : ClanFlag.values()) {
            flags.put(flag, flag.getDefaultValue());
        }
    }

    public boolean getFlag(ClanFlag flag) {
        return flags.getOrDefault(flag, flag.getDefaultValue());
    }

    public boolean getFlag(String flagKey) {
        ClanFlag flag = ClanFlag.fromKey(flagKey);
        return flag != null ? getFlag(flag) : false;
    }

    public void setFlag(ClanFlag flag, boolean value) {
        if (flag != null) {
            flags.put(flag, value);
        }
    }

    public void setFlag(String flagKey, boolean value) {
        ClanFlag flag = ClanFlag.fromKey(flagKey);
        if (flag != null) {
            setFlag(flag, value);
        }
    }

    public Map<ClanFlag, Boolean> getFlags() {
        return Collections.unmodifiableMap(flags);
    }

    public boolean isFriendlyFire() {
        return getFlag(ClanFlag.FRIENDLY_FIRE);
    }

    public void setFriendlyFire(boolean friendlyFire) {
        setFlag(ClanFlag.FRIENDLY_FIRE, friendlyFire);
    }

    public boolean isOpenJoin() {
        return getFlag(ClanFlag.OPEN_JOIN);
    }

    public void setOpenJoin(boolean openJoin) {
        setFlag(ClanFlag.OPEN_JOIN, openJoin);
    }

    public boolean isAllyDamage() {
        return getFlag(ClanFlag.ALLY_DAMAGE);
    }

    public void setAllyDamage(boolean allyDamage) {
        setFlag(ClanFlag.ALLY_DAMAGE, allyDamage);
    }

    public boolean isMemberInvites() {
        return getFlag(ClanFlag.MEMBER_INVITES);
    }

    public void setMemberInvites(boolean memberInvites) {
        setFlag(ClanFlag.MEMBER_INVITES, memberInvites);
    }

    public boolean isVisibleInList() {
        return getFlag(ClanFlag.VISIBLE_IN_LIST);
    }

    public void setVisibleInList(boolean visibleInList) {
        setFlag(ClanFlag.VISIBLE_IN_LIST, visibleInList);
    }

    public boolean isPublicHome() {
        return getFlag(ClanFlag.PUBLIC_HOME);
    }

    public void setPublicHome(boolean publicHome) {
        setFlag(ClanFlag.PUBLIC_HOME, publicHome);
    }

    public boolean isSpyChat() {
        return getFlag(ClanFlag.SPY_CHAT);
    }

    public void setSpyChat(boolean spyChat) {
        setFlag(ClanFlag.SPY_CHAT, spyChat);
    }

    public boolean isAlly(String clanName) {
        return clanName != null && allies.contains(clanName.toLowerCase());
    }

    public Set<String> getAllies() {
        return Collections.unmodifiableSet(allies);
    }

    public void addAlly(String clanName) {
        if (clanName != null) {
            allies.add(clanName.toLowerCase());
        }
    }

    public void removeAlly(String clanName) {
        if (clanName != null) {
            allies.remove(clanName.toLowerCase());
        }
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

    public Set<UUID> getJoinRequests() {
        return Collections.unmodifiableSet(joinRequests);
    }

    public boolean hasJoinRequest(UUID uuid) {
        return joinRequests.contains(uuid);
    }

    public void addJoinRequest(UUID uuid) {
        joinRequests.add(uuid);
    }

    public void removeJoinRequest(UUID uuid) {
        joinRequests.remove(uuid);
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = Math.max(0, kills);
    }

    public void addKill() {
        this.kills++;
    }

    public void addKills(int amount) {
        this.kills += Math.max(0, amount);
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = Math.max(0, deaths);
    }

    public void addDeath() {
        this.deaths++;
    }

    public void addDeaths(int amount) {
        this.deaths += Math.max(0, amount);
    }

    public int getRivalKills() {
        return rivalKills;
    }

    public void setRivalKills(int rivalKills) {
        this.rivalKills = Math.max(0, rivalKills);
    }

    public void addRivalKill() {
        this.rivalKills++;
    }

    public void addRivalKills(int amount) {
        this.rivalKills += Math.max(0, amount);
    }

    public double getKDR() {
        if (deaths <= 0) {
            return (double) kills;
        }
        return (double) kills / deaths;
    }

    public String getFormattedKDR() {
        return String.format(java.util.Locale.US, "%.2f", getKDR());
    }
}
