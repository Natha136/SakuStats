package com.sakustats;

import java.io.Serializable;
import java.util.Date;

public class PlanNotification implements Serializable {
    private String id;
    private String title;
    private String message;
    private Date timestamp;
    private String planId;
    private String type; // e.g., "reminder", "deadline", "progress", "achievement"

    public PlanNotification() {
        // Required for Firebase
    }

    public PlanNotification(String id, String title, String message, Date timestamp, String planId, String type) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.planId = planId;
        this.type = type;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
