package db;

import java.util.List;
import java.util.ArrayList;
import java.sql.*;
import java.util.Optional;

import model.Agent;

public class AgentRepository {

  // READ

  public List<Agent> findAll() throws SQLException {
    List<Agent> agents = new ArrayList<>();
    String sql = "SELECT * FROM agents ORDER BY id";
    try (Statement st = Database.get().createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      while (rs.next())
        agents.add(map(rs));
    }
    return agents;
  }

  public Optional<Agent> findbyId(int id) throws SQLException {
    String sql = "SELECT * FROM agents WHERE id = ?";
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? Optional.of(map(rs)) : Optional.empty();
      }
    }
  }

  // CREATE

  public Agent create(String name, String email, String phone, boolean is_admin, String created_at)
      throws SQLException {
    String sql = "INSERT INTO agents (name, email, phone, is_admin, created_at) VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement ps = Database.get().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, name);
      ps.setString(2, email);
      ps.setString(3, phone);
      ps.setInt(4, is_admin ? 1 : 0);
      ps.setString(5, created_at);
      ps.executeUpdate();
      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next())
          return findbyId(keys.getInt(1)).orElseThrow();
      }
    }
    throw new SQLException("Creation failed");
  }

  // UPDATE

  public Optional<Agent> update(int id, String name, String email, String phone, boolean is_admin, String created_at)
      throws SQLException {
    String sql = "UPDATE agents SET name = ?, email = ?, phone = ?, is_admin = ?, created_at = ? WHERE id = ?";
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setString(1, name);
      ps.setString(2, email);
      ps.setString(3, phone);
      ps.setInt(4, is_admin ? 1 : 0);
      ps.setString(5, created_at);
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
    String sql = "DELETE FROM agents WHERE id = ?";
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setInt(1, id);
      return ps.executeUpdate() > 0;
    }
  }

  // MAPPING

  public Agent map(ResultSet rs) throws SQLException {
    return new Agent(
        rs.getInt("id"),
        rs.getString("name"),
        rs.getString("email"),
        rs.getString("phone"),
        rs.getInt("is_admin") == 1,
        rs.getString("created_at"));
  }
}
