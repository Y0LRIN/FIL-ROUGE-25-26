package db;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.sql.*;

import model.PropertyImage;

public class PropertyImageRepository {

  // READ

  public List<PropertyImage> findAll() throws SQLException {
    List<PropertyImage> propertyImages = new ArrayList<>();
    String sql = "SELECT * FROM property_Images ORDER BY id";
    try (Statement st = Database.get().createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      while (rs.next()) {
        propertyImages.add(map(rs));
      }
    }
    return propertyImages;
  }

  public Optional<PropertyImage> findbyId(int id) throws SQLException {
    String sql = "SELECT * FROM property_Images WHERE id = ?";
    try (PreparedStatement ps = Database.get().prepareStatement(arg0)) {
      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? Optional.of(map(rs)) : Optional.empty();
      }
    }
  }

  // CREATE

  public PropertyImage create(int property_id, String image_url, boolean is_main, String created_at) throws SQLException {
    String sql = "INSERT INTO property_Images (property_id, image_url, is_main, created_at) VALUES (?, ?, ?, ?)";
    try (PreparedStatement ps = Database.get().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)); {
      ps.setInt(1, property_id);
      ps.setString(2, image_url);
      ps.setInt(3, is_main ? 1 : 0);
      ps.setString(4, created_at);
      ps.executeQuery();
      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) {
          return findbyId(keys.getInt(1)).orElseThrow();
        }
      }
    }
    throw new SQLException("Creation failed");
  }
}
