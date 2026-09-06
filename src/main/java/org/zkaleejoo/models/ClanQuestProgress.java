package org.zkaleejoo.models;

public class ClanQuestProgress {

    private final String clanName;
    private final String questId;
    private int progress;
    private boolean completed;
    private final String assignedDate;

    public ClanQuestProgress(String clanName, String questId, int progress, boolean completed, String assignedDate) {
        this.clanName = clanName;
        this.questId = questId;
        this.progress = Math.max(0, progress);
        this.completed = completed;
        this.assignedDate = assignedDate;
    }

    public String getClanName() {
        return clanName;
    }

    public String getQuestId() {
        return questId;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, progress);
    }

    public void addProgress(int amount) {
        this.progress += Math.max(0, amount);
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getAssignedDate() {
        return assignedDate;
    }
}
