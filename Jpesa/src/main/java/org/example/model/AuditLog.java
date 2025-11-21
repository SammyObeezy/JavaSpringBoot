package org.example.model;

import java.time.LocalDateTime;

public class AuditLog implements TimeTracked {

    private Long logId;
    private Long userId;
    private String action;
    private String ipAddress;

    // Fields required by TimeTracked interface
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AuditLog() {}

    public AuditLog(Long userId, String action, String ipAddress) {
        this.userId = userId;
        this.action = action;
        this.ipAddress = ipAddress;

        // Time initialization
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    // --- TimeTracked Implementation ---

    @Override
    public LocalDateTime getCreatedAt() { return createdAt; }
    @Override
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    @Override
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "AuditLog{" +
                "logId=" + logId +
                ", userId=" + userId +
                ", action='" + action + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}