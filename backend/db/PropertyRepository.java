package db;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

import java.sql.*;

import model.Property;
import model.enums.PropertyStatus;
import model.enums.PropertyType;

public class PropertyRepository {

  // READ

  public List<Property> findAll() throws SQLException {
    List<Property> properties = new ArrayList<>();
    String sql = "SELECT * FROM properties ORDER BY id";
    try (Statement st = Database.get().createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      while (rs.next()) {
        properties.add(map(rs));
      }
    }
    return properties;
  }

  public Optional<Property> findbyId(int id) throws SQLException {
    String sql = "SELECT * FROM properties WHERE id = ?";
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? Optional.of(map(rs)) : Optional.empty();
      }
    }
  }

  // CREATE

  public Property create(
      String title,
      String description,
      int price,
      float surface,
      int rooms,
      PropertyType type,
      PropertyStatus status,
      int agent_id,
      int address_id,
      String created_at) throws SQLException {
    String sql = """
        INSERT INTO properties (
            title,
            description,
            price,
            surface,
            rooms,
            type,
            status,
            agent_id,
            address_id,
            created_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
    try (PreparedStatement ps = Database.get().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, title);
      ps.setString(2, description);
      ps.setInt(3, price);
      ps.setFloat(4, surface);
      ps.setInt(5, rooms);
      ps.setString(6, type.name());
      ps.setString(7, status.name());
      ps.setInt(8, agent_id);
      ps.setInt(9, address_id);
      ps.setString(10, created_at);
      ps.executeUpdate();
      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) {
          return findbyId(keys.getInt(1)).orElseThrow();
        }
      }
    }
    throw new SQLException("Creation Failed");
  }

  // UPDATE

  public Optional<Property> update(
      int id,
      String title,
      String description,
      int price,
      float surface,
      int rooms,
      PropertyType type,
      PropertyStatus status,
      int agent_id,
      int address_id,
      String created_at) throws SQLException {
    String sql = """
        UPDATE properties SET
        title = ?,
        description = ?,
        price = ?,
        surface = ?,
        rooms = ?,
        type = ?,
        status = ?,
        agent_id = ?,
        address_id = ?,
        created_at = ?
        WHERE id = ?
        """;
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setString(1, title);
      ps.setString(2, description);
      ps.setInt(3, price);
      ps.setFloat(4, surface);
      ps.setInt(5, rooms);
      ps.setString(6, type.name());
      ps.setString(7, status.name());
      ps.setInt(8, agent_id);
      ps.setInt(9, address_id);
      ps.setString(10, created_at);
      ps.setInt(11, id);
      int affected = ps.executeUpdate();
      if (affected == 0) {
        return Optional.empty();
      }
      return findbyId(id);
    }
  }

  // DELETE

  public boolean delete(int id) throws SQLException {
    String sql = "DELETE FROM properties WHERE id = ?";
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setInt(1, id);
      return ps.executeUpdate() > 0;
    }
  }

  // MAPPING

  public Property map(ResultSet rs) throws SQLException {
    return new Property(
        rs.getInt("id"),
        rs.getString("title"),
        rs.getString("description"),
        rs.getInt("price"),
        rs.getFloat("surface"),
        rs.getInt("rooms"),
        PropertyType.valueOf(rs.getString("type")),
        PropertyStatus.valueOf(rs.getString("status")),
        rs.getInt("agent_id"),
        rs.getInt("address_id"),
        rs.getString("created_at"));
  }
}
