package org.example.model;

import java.time.LocalDateTime;

public class User implements TimeTracked {

    private Long userId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String passwordHash;
    private UserStatus status;

    // Fields required by TimeTracked interface
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Required no-argument constructor for Jackson (JSON) and reflection
    public User() {}

    public User(String fullName, String phoneNumber, String email, String passwordHash){
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = UserStatus.ACTIVE;
        // Time initialization
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    // Getters and Setters

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName(){
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserStatus getStatus(){
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    // TimeTracked Implementation
    @Override
    public LocalDateTime getCreatedAt(){
        return createdAt;
    }
    @Override
    public void setCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
    }

    @Override
    public LocalDateTime getUpdatedAt(){
        return updatedAt;
    }
    @Override
    public void setUpdatedAt(LocalDateTime updatedAt){
        this.updatedAt = updatedAt;
    }
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", fullName='" + fullName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
