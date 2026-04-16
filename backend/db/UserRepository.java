package db;

import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {

  // READ

  public List<User> findAll() throws SQLException {
    List<User> users = new ArrayList<>();
    String sql = "SELECT id, name, email, created_at FROM users ORDER BY id";
    try (Statement st = Database.get().createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      while (rs.next())
        users.add(map(rs));
    }
    return users;
  }

  public Optional<User> findbyId(int id) throws SQLException {
    String sql = "SELECT id, name, email, created_at FROM users WHERE id = ?";
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? Optional.of(map(rs)) : Optional.empty();
      }
    }
  }

  // CREATE

  public User create(String name, String email) throws SQLException {
    String sql = "INSERT INTO users (name, email) VALUES (?, ?)";
    try (PreparedStatement ps = Database.get().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, name);
      ps.setString(2, email);
      ps.executeUpdate();
      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next())
          return findbyId(keys.getInt(1)).orElseThrow();
      }
    }
    throw new SQLException("Creation failed");
  }

  // UPDATE

  public Optional<User> update(int id, String name, String email) throws SQLException {
    String sql = "UPDATE users SET name = ?, email = ? WHERE id = ?";
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setString(1, name);
      ps.setString(2, email);
      ps.setInt(3, id);
      int affected = ps.executeUpdate();
      if (affected == 0)
        return Optional.empty();
    }
    return findbyId(id);
  }

  // DELETE

  public boolean delete(int id) throws SQLException {
    String sql = "DELETE FROM users WHERE id = ?";
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setInt(1, id);
      return ps.executeUpdate() > 0;
    }
  }

  // MAPPING

  private User map(ResultSet rs) throws SQLException {
    return new User(
        rs.getInt("id"),
        rs.getString("name"),
        rs.getString("email"),
        rs.getString("created_at"));
  }
}
