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
        users.add(map(rs));
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
        )
        """;
    try (PreparedStatement ps = Database.get().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setInt(1, contract_id);
      ps.setInt(2, amount);
      ps.setString(3, payment_date);
      ps.setString(4, payment_method);
    }
  }
}
