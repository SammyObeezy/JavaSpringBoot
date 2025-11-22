package org.example.repository;

import org.example.config.DatabaseConfig;
import org.example.model.OtpCode;
import org.example.model.OtpPurpose;

import java.sql.*;
import java.util.Optional;

public class OtpRepository {

    private static final String INSERT_OTP = "INSERT INTO otp_codes (user_id, otp_code, purpose, expires_at, is_used, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
    // Find valid OTP: Matches User, Code, Not Used, and Not Expired
    private static final String FIND_VALID_OTP =
            "SELECT * FROM otp_codes WHERE user_id = ? AND otp_code = ? AND is_used = false AND expires_at > CURRENT_TIMESTAMP";

    private static final String MARK_AS_USED = "UPDATE otp_codes SET is_used = true, updated_at = ? WHERE otp_id = ?";


    public void save(OtpCode otp) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_OTP)){

            stmt.setLong(1, otp.getUserId());
            stmt.setString(2, otp.getOtpCode());
            stmt.setString(3, otp.getPurpose().name());
            stmt.setTimestamp(4, Timestamp.valueOf(otp.getExpiresAt()));
            stmt.setBoolean(5, otp.isUsed());
            stmt.setTimestamp(6, Timestamp.valueOf(otp.getCreatedAt()));
            stmt.setTimestamp(7, Timestamp.valueOf(otp.getUpdatedAt()));

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving OTP: " + e.getMessage(), e);
        }
    }
    public Optional<OtpCode> findValidOtp(Long userId, String code) {
        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(FIND_VALID_OTP)){

            stmt.setLong(1, userId);
            stmt.setString(2, code);

            try (ResultSet rs = stmt.executeQuery()){
                if (rs.next()){
                    return Optional.of(mapRowToOtp(rs));
                }
            }
            } catch (SQLException e) {
            throw new RuntimeException("Error checking OTP: " + e.getMessage(), e);
        }
        return Optional.empty();
    }
    public void markAsUsed(Long otpId) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(MARK_AS_USED)) {

            stmt.setTimestamp(1, Timestamp.valueOf(java.time.LocalDateTime.now()));
            stmt.setLong(2, otpId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating OTP status", e);
        }
    }
    public OtpCode mapRowToOtp(ResultSet rs) throws SQLException {
        OtpCode otp = new OtpCode();
        otp.setOtpId(rs.getLong("otp_id"));
        otp.setUserId(rs.getLong("user_id"));
        otp.setOtpCode(rs.getString("otp_code"));
        otp.setPurpose(OtpPurpose.valueOf(rs.getString("purpose")));
        otp.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
        otp.setUsed(rs.getBoolean("is_used"));
        return otp;
    }
}
