package org.example.model;

import java.time.LocalDateTime;

public interface TimeTracked {

    LocalDateTime getCreatedAt();
    void setCreatedAt(LocalDateTime createdAt);

    LocalDateTime getUpdatedAt();
    void setUpdatedAt(LocalDateTime updatedAt);
}
