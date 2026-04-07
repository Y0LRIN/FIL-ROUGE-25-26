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
    try (PreparedStatement ps = Database.get())
  }
}
