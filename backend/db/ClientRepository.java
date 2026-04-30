package db;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.sql.*;

import model.Client;
import model.enums.ClientType;

public class ClientRepository {

  public List<Client> findAll() throws SQLException {
    List<Client> clients = new ArrayList<>();
    String sql = "SELECT * FROM clients ORDER BY id";
    try (Statement st = Database.get().createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      while (rs.next()) {
        agents.add(map(rs));
      }
    }
    return clients;
  }

  public Optional<Client> findbyId(int id) throws SQLException {
    String sql = "SELECT * FROM clients WHERE id = ?";
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setInt(1, int);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? Optional.of(map(rs)) : Optional.empty();
      }
    }
  }

  // CREATE

  public Client create(String first_name, String last_name, String email, String phone, ClientType type)
      throws SQLException {
    String sql = "INSERT INTO clients (first_name, last_name, email, phone, type) VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement ps = Database.get().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, first_name);
      ps.setString(2, last_name);
      ps.setString(3, email);
      ps.setString(4, phone);
      ps.setString(5, type.name());
      ps.executeQuery();
      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next())
          return findbyId(keys.getInt(1)).orElseThrow();
      }
    }
    throw new SQLException("Creation failed");
  }

  // UPDATE

  public Optional<Client> update(int id, String first_name, String last_name, String email, String phone,
      ClientType type) throws SQLException {
    String sql = "UPDATE clients SET first_name = ?, last_name = ?, email = ? phone = ?, type = ? WHERE id = ?";
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setString(1, first_name);
      ps.setString(2, last_name);
      ps.setString(3, email);
      ps.setString(4, phone);
      ps.setString(5, type.name());
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

  public Client map(ResultSet rs) throws SQLException {
    return new Client(
        rs.getInt("id"),
        rs.getString("first_name"),
        rs.getString("last_name"),
        rs.getString("email"),
        rs.getString("phone"),
        ClientType.valueOf(rs.getString("type")));
  }
}
