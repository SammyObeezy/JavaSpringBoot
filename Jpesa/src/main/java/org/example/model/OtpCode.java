package org.example.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class OtpCode implements TimeTracked{

    private Long otpId;
    private Long userId;
    private String otpCode;
    private OtpPurpose purpose;
    private LocalDateTime expiresAt;
    private boolean isUsed;

    // Fields required by TimeTracked interface
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;

    public OtpCode(){}

    public OtpCode(Long userId, String otpCode, OtpPurpose purpose, int validityMinutes){
        this.userId = userId;
        this.otpCode = otpCode;
        this.purpose = purpose;
        this.isUsed = false;

        // Time initialization
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        //Set expiration based on creation time
        this.expiresAt = this.createdAt.plusMinutes(validityMinutes);
    }

    // --- Getters and Setters ---

    public Long getOtpId() { return otpId; }
    public void setOtpId(Long otpId) { this.otpId = otpId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public OtpPurpose getPurpose() { return purpose; }
    public void setPurpose(OtpPurpose purpose) { this.purpose = purpose; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public boolean isUsed() { return isUsed; }
    public void setUsed(boolean used) { isUsed = used; }

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
        return "OtpCode{" +
                "otpId=" + otpId +
                ", userId=" + userId +
                ", otpCode='" + otpCode + '\'' +
                ", purpose=" + purpose +
                ", expiresAt=" + expiresAt +
                ", isUsed=" + isUsed +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
