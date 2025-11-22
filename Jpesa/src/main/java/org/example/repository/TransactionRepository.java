package org.example.repository;

import org.example.config.DatabaseConfig;
import org.example.model.Transaction;
import org.example.model.TransactionType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {

    private static String INSERT_TXN = "INSERT INTO transactions (wallet_id, txn_type, amount, reference_code, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?) RETURNING transaction_id";

    // Fetch last 10 transactions for a specific wallet, newest first
    private static final String FIND_MINI_STATEMENT = "SELECT * FROM transactions WHERE wallet_id = ? ORDER BY created_at DESC LIMIT 10";

    public Transaction save(Transaction txn){
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_TXN)){

            stmt.setLong(1, txn.getWalletId());
            stmt.setString(2, txn.getTxnType().name());
            stmt.setBigDecimal(3, txn.getAmount());
            stmt.setString(4, txn.getReferenceCode());
            stmt.setTimestamp(5, Timestamp.valueOf(txn.getCreatedAt()));
            stmt.setTimestamp(6, Timestamp.valueOf(txn.getUpdatedAt()));

            try (ResultSet generatedKeys = stmt.executeQuery()){
                if (generatedKeys.next()){
                    txn.setTransactionId(generatedKeys.getLong(1));
                }
            }
            return txn;
        } catch (SQLException e){
            throw new RuntimeException("Error saving transaction: " + e.getMessage(), e);
        }
    }
    public List<Transaction> findMiniStatement(Long walletId){
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(FIND_MINI_STATEMENT)) {

            stmt.setLong(1, walletId);
            try (ResultSet rs = stmt.executeQuery()){
                while (rs.next()) {
                    transactions.add(mapRowToTransaction(rs));
                }
            }
        } catch (SQLException e){
            throw new RuntimeException("Error fetching mini statement", e);
        }
        return transactions;
    }

    private Transaction mapRowToTransaction(ResultSet rs) throws SQLException {
        Transaction txn = new Transaction();
        txn.setTransactionId(rs.getLong("transaction_id"));
        txn.setWalletId(rs.getLong("wallet_id"));
        txn.setTxnType(TransactionType.valueOf(rs.getString("txn_type")));
        txn.setAmount(rs.getBigDecimal("amount"));
        txn.setReferenceCode(rs.getString("reference_code"));
        txn.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return txn;
    }
}
