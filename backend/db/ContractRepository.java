package db;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.sql.*;

import model.Contract;
import model.enums.ContractType;

public class ContractRepository {

  // READ

  public List<Contract> findAll() throws SQLException {
    List<Contract> contracts = new ArrayList<>();
    String sql = "SELECT * FROM contracts ORDER BY id";
    try (Statement st = Database.get().createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      while (rs.next()) {
        contracts.add(map(rs));
      }
    }
    return contracts;
  }

  public Optional<Contract> findbyId(int id) throws SQLException {
    String sql = "SELECT * FROM contracts WHERE id = ?";
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? Optional.of(map(rs)) : Optional.empty();
      }
    }
  }

  // CREATE

  public Contract create(int property_id, int client_id, int agent_id, ContractType type, String signed_at)
      throws SQLException {
    String sql = "INSERT INTO contracts (property_id, client_id, agent_id, type, signed_at) VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement ps = Database.get().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setInt(1, property_id);
      ps.setInt(2, client_id);
      ps.setInt(3, agent_id);
      ps.setString(4, type.name());
      ps.setString(5, signed_at);
      ps.executeQuery();
      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) {
          return findbyId(keys.getInt(1)).orElseThrow();
        }
      }
    }
    throw new SQLException("Creation failed");
  }

  // UPDATE

  public Optional<Contract> update(int id, int property_id, int client_id, int agent_id, ContractType type,
      String signed_at) throws SQLException {
    String sql = "UPDATE contracts SET property_id = ?, client_id = ?, agent_id = ? type = ?, signed_at = ? WHERE id = ?";
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setInt(1, property_id);
      ps.setInt(2, client_id);
      ps.setInt(3, agent_id);
      ps.setString(4, type.name());
      ps.setString(5, signed_at);
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
    String sql = "DELETE FROM contracts WHERE id = ?";
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setInt(1, id);
      return ps.executeUpdate() > 0;
    }
  }

  // MAPPING

  public Contract map(ResultSet rs) throws SQLException {
    return new Contract(
        rs.getInt("id"),
        rs.getInt("property_id"),
        rs.getInt("client_id"),
        rs.getInt("agent_id"),
        ContractType.valueOf(rs.getString("type")),
        rs.getString("signed_at"));
  }
}
