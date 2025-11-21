package org.example.repository;

import org.example.config.DatabaseConfig;
import org.example.model.User;
import org.example.model.UserStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class UserRepository {

    // SQL Queries
    private static final String INSERT_USER = "INSERT INTO users (full_name, phone_number, email, password_hash, status, created_at, updated_at) VALUES (?,?,?,?,?,?,?) RETURNING user_id";
    private static final String FIND_BY_PHONE = "SELECT * FROM users WHERE phone_number = ?";
    private static final String FIND_BY_ID = "SELECT * FROM users WHERE user_id = ?";
    private static final String UPDATE_STATUS = "UPDATE users SET status = ?, updated_at = ? WHERE user_id = ?";

    /**
     * Saves a new user to the database.
     * Updates the user object with the generated ID.
     */
    public User save(User user){
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_USER)){

            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getPhoneNumber());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPasswordHash());
            stmt.setString(5, user.getStatus().name());
            stmt.setTimestamp(6, Timestamp.valueOf(user.getCreatedAt()));
            stmt.setTimestamp(7, Timestamp.valueOf(user.getUpdatedAt()));

            try (ResultSet generatedKeys = stmt.executeQuery()){
                if (generatedKeys.next()){
                    user.setUserId(generatedKeys.getLong(1));
                } else{
                    throw new SQLException("Creating user failed, no ID obtained");
                }
            }
            return user;
        } catch (SQLException e){
            throw new RuntimeException("Error saving user: " + e.getMessage(), e);
        }
    }
    public Optional<User> findByPhoneNumber(String phoneNumber){
        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(FIND_BY_PHONE)){

            stmt.setString(1, phoneNumber);
            try (ResultSet rs = stmt.executeQuery()){
                if (rs.next()){
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (SQLException e){
            throw new RuntimeException("Error finding user by phone: " + e.getMessage());
        }
        return Optional.empty();
    }
    public Optional<User> findById(Long id){
        try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID)){

            stmt.setLong(1, id);
            try (ResultSet rs= stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        }catch (SQLException e){
            throw new RuntimeException("Error finding user by ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }
    /*
    *Updates the user status (e.g LOCKING a user after failed attempts
     */
    public void updateStatus(Long userId, UserStatus status){
        try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(UPDATE_STATUS)){

            stmt.setString(1, status.name());
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(3, userId);
            stmt.executeUpdate();
        } catch (SQLException e){
            throw new RuntimeException("Error in updating status", e);
        }
    }
    // Helper method to keep code DRY
    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getLong("user_id"));
        user.setFullName(rs.getString("full_name"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setStatus(UserStatus.valueOf(rs.getString("status")));

        // Handle TimeStamps
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) user.setCreatedAt(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) user.setUpdatedAt(updated.toLocalDateTime());

        return user;
    }
}
