package db;

import model.Visit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VisitRepository {

  // READ

  public List<Visit> findAll() throws SQLException {
    List<Visit> visits = new ArrayList<>();
    String sql = "SELECT * FROM visits ORDER BY id";
    try (Statement st = Database.get().createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      while (rs.next()) {
        visits.add(map(rs));
      }
    }
    return visits;
  }

  public Optional<Visit> findbyId(int id) throws SQLException {
    String sql = "SELECT * FROM visits WHERE id = ?";
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? Optional.of(map(rs)) : Optional.empty();
      }
    }
  }

  // CREATE

  public Visit create(
      int property_id,
      int client_id,
      int agent_id,
      String visit_date,
      String feedback) throws SQLException {
    String sql = """
            INSERT INTO visits (
            property_id,
            client_id,
            agent_id,
            visit_date,
            feedback)
            VALUES (?, ?, ?, ?, ?)
        """;
    try (PreparedStatement ps = Database.get().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setInt(1, property_id);
      ps.setInt(2, client_id);
      ps.setInt(3, agent_id);
      ps.setString(4, visit_date);
      ps.setString(5, feedback);
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

  public Optional<Visit> update(
      int id,
      int property_id,
      int client_id,
      int agent_id,
      String visit_date,
      String feedback) throws SQLException {
    String sql = """
          UPDATE visits SET
            property_id = ?,
            client_id = ?,
            agent_id = ?,
            visit_date = ?,
            feedback = ?
        WHERE id = ?
        """;
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setInt(1, property_id);
      ps.setInt(2, client_id);
      ps.setInt(3, agent_id);
      ps.setString(4, visit_date);
      ps.setString(5, feedback);
      ps.setInt(6, id);
      int affected = ps.executeUpdate();
      if (affected == 0) {
        return Optional.empty();
      }
    }
    return findbyId(id);
  }

  // DELETE

  public boolean delete(int id) throws SQLException {
    String sql = "DELETE FROM visits WHERE id = ?";
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setInt(1, id);
      return ps.executeUpdate() > 0;
    }
  }

  // MAPPING

  private Visit map(ResultSet rs) throws SQLException {
    return new Visit(
        rs.getInt("id"),
        rs.getInt("property_id"),
        rs.getInt("client_id"),
        rs.getInt("agent_id"),
        rs.getString("visit_date"),
        rs.getString("feedback"));
  }
}
