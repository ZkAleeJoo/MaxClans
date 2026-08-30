package org.zkaleejoo.models;

public enum MemberSortType {
    ROLE,
    KDR,
    PLAYTIME,
    JOIN_RECENT,
    JOIN_OLDEST;

    public MemberSortType next() {
        MemberSortType[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }

    public MemberSortType previous() {
        MemberSortType[] values = values();
        return values[(this.ordinal() - 1 + values.length) % values.length];
    }
}
