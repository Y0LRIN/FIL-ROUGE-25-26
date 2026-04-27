package db;

import model.Address;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AddressRepository {

  // READ

  public List<Address> findAll() throws SQLException {
    List<Address> addresses = new ArrayList<>();
    String sql = "SELECT * FROM addresses ORDER BY id";
    try (Statement st = Database.get().createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      while (rs.next())
        addresses.add(map(rs));
    }
    return addresses;
  }

  public Optional<Address> findbyId(int id) throws SQLException {
    String sql = "SELECT * FROM addresses WHERE id = ?";
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? Optional.of(map(rs)) : Optional.empty();
      }
    }
  }

  // CREATE

  public Address create(String street, String city, int postal_code, String country) throws SQLException {
    String sql = "INSERT INTO addresses (street, city, postal_code, county) VALUES (?, ?, ?, ?)";
    try (PreparedStatement ps = Database.get().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, street);
      ps.setString(2, city);
      ps.setInt(3, postal_code);
      ps.setString(4, country);
      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next())
          return findbyId(keys.getInt(1)).orElseThrow();
      }
    }
    throw new SQLException("Creation failed");
  }

  // UPDATE

  public Optional<Address> update(int id, String street, String city, int postal_code, String country)
      throws SQLException {
    String sql = "UPDATE addresses SET street = ?, city = ?, postal_code = ?, country = ? WHERE id = ?";
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setString(1, street);
      ps.setString(2, city);
      ps.setInt(3, postal_code);
      ps.setString(4, country);
      ps.setInt(5, id);
      int affected = ps.executeUpdate();
      if (affected == 0) {
        return Optional.empty();
      }
      return findbyId(id);
    }
  }

  // DELETE

  public boolean delete(int id) throws SQLException {
    String sql = "DELETE FROM addresses WHERE id = ?";
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setInt(1, id);
      return ps.executeUpdate() > 0;
    }
  }

  // MAPPING

  private Address map(ResultSet rs) throws SQLException {
    return new Address(
        rs.getInt("id"),
        rs.getString("street"),
        rs.getString("city"),
        rs.getInt("postal_code"),
        rs.getString("country"));
  }
}
