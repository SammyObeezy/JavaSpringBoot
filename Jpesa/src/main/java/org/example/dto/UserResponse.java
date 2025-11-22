package org.example.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.example.model.User;

import java.time.LocalDateTime;

public class UserResponse {
    private Long userId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;


    // Constructor that takes a User model and strips the password
    public UserResponse(User user){
        this.userId = user.getUserId();
        this.fullName = user.getFullName();
        this.phoneNumber = user.getPhoneNumber();
        this.email = user.getEmail();
        this.status = user.getStatus().name();
        this.createdAt = user.getCreatedAt();
    }

    // Getters only (Response objects are usually read-only)

    public Long getUserId() {
        return userId;
    }
    public String getFullName() {
        return fullName;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public String getEmail() {
        return email;
    }
    public String getStatus(){
        return status;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
