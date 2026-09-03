package org.zkaleejoo.models;

public enum ClanFlag {

    FRIENDLY_FIRE("friendly_fire", false, false),
    OPEN_JOIN("open_join", false, false),
    ALLY_DAMAGE("ally_damage", false, false),
    MEMBER_INVITES("member_invites", false, false),
    VISIBLE_IN_LIST("visible_in_list", true, false),
    PUBLIC_HOME("public_home", false, false),
    SPY_CHAT("spy_chat", false, true);

    private final String key;
    private final boolean defaultValue;
    private final boolean adminOnly;

    ClanFlag(String key, boolean defaultValue, boolean adminOnly) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.adminOnly = adminOnly;
    }

    public String getKey() {
        return key;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }

    public boolean isAdminOnly() {
        return adminOnly;
    }

    public static ClanFlag fromKey(String key) {
        if (key == null) return null;
        String clean = key.trim().toLowerCase().replace("-", "_").replace(" ", "_");
        if (clean.equals("ff") || clean.equals("friendlyfire")) {
            return FRIENDLY_FIRE;
        }
        if (clean.equals("openjoin")) {
            return OPEN_JOIN;
        }
        if (clean.equals("allydamage")) {
            return ALLY_DAMAGE;
        }
        if (clean.equals("memberinvites") || clean.equals("invites")) {
            return MEMBER_INVITES;
        }
        if (clean.equals("visibleinlist") || clean.equals("visible") || clean.equals("visibility")) {
            return VISIBLE_IN_LIST;
        }
        if (clean.equals("publichome")) {
            return PUBLIC_HOME;
        }
        if (clean.equals("spychat") || clean.equals("spy")) {
            return SPY_CHAT;
        }
        for (ClanFlag flag : values()) {
            if (flag.key.equalsIgnoreCase(clean) || flag.name().equalsIgnoreCase(clean)) {
                return flag;
            }
        }
        return null;
    }
}
