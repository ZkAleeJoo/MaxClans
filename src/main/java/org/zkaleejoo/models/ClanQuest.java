package org.zkaleejoo.models;

public class ClanQuest {

    public enum QuestType {
        KILL_MOB,
        MINE_BLOCK,
        CHOP_WOOD,
        PVP_KILLS,
        DONATE_BANK;

        public static QuestType fromString(String str) {
            if (str == null) return KILL_MOB;
            try {
                return valueOf(str.toUpperCase());
            } catch (IllegalArgumentException e) {
                return KILL_MOB;
            }
        }
    }

    private final String id;
    private final String name;
    private final String description;
    private final QuestType type;
    private final String target;
    private final int required;
    private final int rewardExp;
    private final String iconMaterial;

    public ClanQuest(String id, String name, String description, QuestType type, String target, int required, int rewardExp, String iconMaterial) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.target = target != null ? target.toUpperCase() : "ANY";
        this.required = Math.max(1, required);
        this.rewardExp = Math.max(0, rewardExp);
        this.iconMaterial = iconMaterial != null ? iconMaterial : "BOOK";
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public QuestType getType() {
        return type;
    }

    public String getTarget() {
        return target;
    }

    public int getRequired() {
        return required;
    }

    public int getRewardExp() {
        return rewardExp;
    }

    public String getIconMaterial() {
        return iconMaterial;
    }
}
