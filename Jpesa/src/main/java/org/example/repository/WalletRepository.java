package org.example.repository;

import org.example.config.DatabaseConfig;
import org.example.model.Wallet;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class WalletRepository {

    // SQL Queries
    private static final String INSERT_WALLET = "INSERT INTO wallets (user_id, balance, currency, created_at, updated_at) VALUES (?,?,?,?,?) RETURNING wallet_id";
    private static final String FIND_BY_USER_ID = "SELECT * FROM wallets WHERE user_id = ?";
    private static final String UPDATE_BALANCE = "UPDATE wallets SET balance = ?, updated_at = ? WHERE wallet_id = ?";

    /*
    * Creates a new wallet for a user.
    * Usually called immediately after creating a User.
     */
    public Wallet save(Wallet wallet){
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_WALLET)){

            stmt.setLong(1, wallet.getUserId());
            stmt.setBigDecimal(2, wallet.getBalance());
            stmt.setString(3, wallet.getCurrency());
            stmt.setTimestamp(4, Timestamp.valueOf(wallet.getCreatedAt()));
            stmt.setTimestamp(5, Timestamp.valueOf(wallet.getUpdatedAt()));

            try (ResultSet generatedKeys = stmt.executeQuery()) {
                if (generatedKeys.next()){
                    wallet.setWalletId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating wallet failed, no ID obtained.");
                }
            }
            return wallet;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving wallet: " + e.getMessage(), e);
        }
    }

    public Optional<Wallet> findByUserId(Long userId) {
        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(FIND_BY_USER_ID)){

            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToWallet(rs));
                }
            }
        } catch (SQLException e){
            throw new RuntimeException("Error finding wallet for user ID: " + userId, e);
        }
        return Optional.empty();
    }

    /**
     * Updates the wallet balance.
     * CRITICAL: This method should often be part of a larger Transaction to ensure safety.
     */
    public void updateBalance(Long walletId, BigDecimal newBalance){
        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(UPDATE_BALANCE)){


            stmt.setBigDecimal(1, newBalance);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(3, walletId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating wallet balance", e);
        }
    }

    private Wallet mapRowToWallet(ResultSet rs) throws SQLException {
        Wallet wallet = new Wallet();
        wallet.setWalletId(rs.getLong("wallet_id"));
        wallet.setUserId(rs.getLong("user_id"));
        wallet.setBalance(rs.getBigDecimal("balance"));
        wallet.setCurrency(rs.getString("currency"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) wallet.setCreatedAt(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) wallet.setUpdatedAt(updated.toLocalDateTime());

        return wallet;
    }
}
