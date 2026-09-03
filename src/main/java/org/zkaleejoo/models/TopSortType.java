package org.zkaleejoo.models;

public enum TopSortType {
    KDR("kdr"),
    KILLS("kills"),
    MEMBERS("members");

    private final String key;

    TopSortType(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public TopSortType next() {
        TopSortType[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }

    public TopSortType previous() {
        TopSortType[] values = values();
        return values[(this.ordinal() - 1 + values.length) % values.length];
    }

    public static TopSortType fromKey(String key) {
        if (key == null) return null;
        String clean = key.trim().toLowerCase();
        for (TopSortType type : values()) {
            if (type.key.equalsIgnoreCase(clean) || type.name().equalsIgnoreCase(clean)) {
                return type;
            }
        }
        if (clean.equals("member") || clean.equals("size")) {
            return MEMBERS;
        }
        if (clean.equals("kill")) {
            return KILLS;
        }
        if (clean.equals("ratio")) {
            return KDR;
        }
        return null;
    }
}
