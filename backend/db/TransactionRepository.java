package db;

import model.Transaction;
import model.enums.TransactionStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionRepository {

  // READ

  public List<Transaction> findAll() throws SQLException {
    List<Transaction> transactions = new ArrayList<>();
    String sql = "SELECT * FROM transactions ORDER BY id";
    try (Statement st = Database.get().createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      while (rs.next()) {
        transactions.add(map(rs));
      }
    }
    return transactions;
  }

  public Optional<Transaction> findbyId(int id) throws SQLException {
    String sql = "SELECT * FROM transactions WHERE id = ?";
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? Optional.of(map(rs)) : Optional.empty();
      }
    }
  }

  public Transaction create(
      int contract_id,
      int amount,
      String payment_date,
      String payment_method,
      TransactionStatus status) throws SQLException {
    String sql = """
          INSERT INTO transactions (
            contract_id,
            amount,
            payment_date,
            payment_method,
            status
          ) VALUES (?, ?, ?, ?, ?)
        """;
    try (PreparedStatement ps = Database.get().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setInt(1, contract_id);
      ps.setInt(2, amount);
      ps.setString(3, payment_date);
      ps.setString(4, payment_method);
      ps.setString(5, status.name());
      ps.executeUpdate();
      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) {
          return findbyId(keys.getInt(1)).orElseThrow();
        }
      }
    }
    throw new SQLException("Creation failed");
  }

  // UPDATE

  public Optional<Transaction> update(
      int id,
      int contract_id,
      int amount,
      String payment_date,
      String payment_method,
      TransactionStatus status) throws SQLException {
    String sql = """
          UPDATE transactions SET
            contract_id = ?,
            amount = ?,
            payment_date = ?,
            payment_method = ?,
            status = ?
        WHERE id = ?
        """;
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setInt(1, contract_id);
      ps.setInt(2, amount);
      ps.setString(3, payment_date);
      ps.setString(4, payment_method);
      ps.setString(5, status.name());
      ps.setInt(6, id);
      int affected = ps.executeUpdate();
      if (affected == 0) {
        return Optional.empty();
      }
      return findbyId(id);
    }
  }

  // DELETE

  public boolean delete(int id) throws SQLException {
    String sql = "DELETE FROM transactions WHERE id = ?";
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setInt(1, id);
      return ps.executeUpdate() > 0;
    }
  }

  // MAPPING

  public Transaction map(ResultSet rs) throws SQLException {
    return new Transaction(
        rs.getInt("id"),
        rs.getInt("contract_id"),
        rs.getInt("amount"),
        rs.getString("payment_date"),
        rs.getString("payment_method"),
        TransactionStatus.valueOf(rs.getString("status")));
  }
}
