package db;

import model.Task;
import sun.jvm.hotspot.oops.Array;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskRepository {

  // FIND

  public List<Task> findByUser(int userId) throws SQLException {
    List<Task> tasks = ArrayList<>();
    String sql = "SELECT id, user_id, title, done FROM tasks WHERE user_id = ? ORDER BY id";
    try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
      ps.setInt(1, userId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) tasks.add(map(rs));
      }
    }
  }
}
