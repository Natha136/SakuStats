package com.sakustats;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Plan implements Serializable {
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);
    public static final List<Plan> DATA = new ArrayList<>();

    private long id;
    private String title;
    private String category;
    private long goalAmount;
    private long currentAmount;
    private Date achievementDate;
    private int iconResId;
    private String documentId;
    private Date lastSavingDate;
    private String savingFrequency; // "daily" or "monthly"

    public Plan() {
        this.id = ID_GENERATOR.getAndIncrement();
        this.savingFrequency = "daily"; // default
    }

    public Plan(String title, String category, long goalAmount, long currentAmount, Date achievementDate, int iconResId) {
        this();
        this.title = title;
        this.category = category;
        this.goalAmount = goalAmount;
        this.currentAmount = currentAmount;
        this.achievementDate = achievementDate;
        this.iconResId = iconResId;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public long getGoalAmount() { return goalAmount; }
    public void setGoalAmount(long goalAmount) { this.goalAmount = goalAmount; }
    public long getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(long currentAmount) { this.currentAmount = currentAmount; }
    public Date getAchievementDate() { return achievementDate; }
    public void setAchievementDate(Date achievementDate) { this.achievementDate = achievementDate; }
    public int getIconResId() { return iconResId; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getSavingFrequency() {
        return savingFrequency != null ? savingFrequency : "daily";
    }

    public void setSavingFrequency(String savingFrequency) {
        this.savingFrequency = savingFrequency;
    }

    public int getProgress() {
        if (goalAmount <= 0) return 0;
        return (int) ((currentAmount * 100) / goalAmount);
    }

    public long getRemainingDays() {
        long diff = achievementDate.getTime() - System.currentTimeMillis();
        if (diff <= 0) return 0;
        return diff / (1000 * 60 * 60 * 24);
    }

    public long getRemainingMonths() {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.setTime(achievementDate);
        
        int diffYear = end.get(Calendar.YEAR) - start.get(Calendar.YEAR);
        int diffMonth = diffYear * 12 + end.get(Calendar.MONTH) - start.get(Calendar.MONTH);
        
        return Math.max(0, diffMonth);
    }

    public long getSavingTarget() {
        long remainingMoney = goalAmount - currentAmount;
        if (remainingMoney <= 0) return 0;

        if ("monthly".equals(getSavingFrequency())) {
            long months = getRemainingMonths();
            if (months <= 0) return remainingMoney;
            return remainingMoney / months;
        } else {
            long days = getRemainingDays();
            if (days <= 0) return remainingMoney;
            return remainingMoney / days;
        }
    }

    public Date getLastSavingDate() {
        return lastSavingDate;
    }

    public void setLastSavingDate(Date lastSavingDate) {
        this.lastSavingDate = lastSavingDate;
    }

    public boolean hasSavedToday() {

        if (lastSavingDate == null)
            return false;

        java.util.Calendar today =
                java.util.Calendar.getInstance();

        java.util.Calendar save =
                java.util.Calendar.getInstance();

        save.setTime(lastSavingDate);

        return today.get(java.util.Calendar.YEAR)
                ==
                save.get(java.util.Calendar.YEAR)

                &&

                today.get(java.util.Calendar.DAY_OF_YEAR)
                        ==
                        save.get(java.util.Calendar.DAY_OF_YEAR);
    }
}
