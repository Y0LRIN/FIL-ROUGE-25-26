package db;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.sql.*;

import model.Favorite;

public class FavoriteRepository {

  public List<Favorite> findAll() throws SQLException {
    List<Favorite> favorites = new ArrayList<>();
    String sql = "SELECT * FROM favorites ORDER BY id";
    try (Statement st = Database.get().createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      while (rs.next()) {
        favorites.add(map(rs));
      }
    }
    return favorites;
  }

  public Optional<Favorite> findbyId(int id) throws SQLException {
    String sql = "SEELCT * FROM favorites WHERE is = ?";
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? Optional.of(map(rs)) : Optional.empty();
      }
    }
  }

  // CREATE

  public Favorite create(int client_id, int property_id, String created_at) throws SQLException {
    String sql = "INSERT INTO favorites (client_id, property_id, created_at) VALUES (?, ?, ?)";
    try (PreparedStatement ps = Database.get().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setInt(1, client_id);
      ps.setInt(2, property_id);
      ps.setString(3, created_at);
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

  public Optional<Favorite> update(int id, int client_id, int property_id, String created_at) throws SQLException {
    String sql = "UPDATE favorites SET client_id = ?, property_id = ?, created_at = ? WHERE id = ?";
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setInt(1, client_id);
      ps.setInt(2, property_id);
      ps.setString(3, created_at);
      ps.setInt(4, id);
      int affected = ps.executeUpdate();
      if (affected == 0) {
        return Optional.empty();
      }
      return findbyId(id);
    }
  }

  // DELETE

  public boolean delete(int id) throws SQLException {
    String sql = "DELETE FROM favorites WHERE id = ?";
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setInt(1, id);
      return ps.executeUpdate() > 0;
    }
  }

  // MAPPING

  public Favorite map(ResultSet rs) throws SQLException {
    return new Favorite(
        rs.getInt("id"),
        rs.getInt("client_id"),
        rs.getInt("property_id"),
        rs.getString("created_at"));
  }
}
