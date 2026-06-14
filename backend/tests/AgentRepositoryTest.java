import db.AgentRepository;
import db.Database;
import model.Agent;

import java.sql.SQLException;
import java.util.Optional;
import java.util.List;

public class AgentRepositoryTest {

  public static void main(String[] args) throws Exception {
    Database.init("jdbc:sqlite::memory:");
    try {
      testCreateAndFindById();
      testUpdateAndDelete();
      System.out.println("AgentRepositoryTest passed");
    } finally {
      Database.close();
    }
  }

  private static void testCreateAndFindById() throws SQLException {
    AgentRepository repo = new AgentRepository();
    Agent created = repo.create("Agent X", "agentx@example.com", "0123456789", true, "2026-06-14");
    Optional<Agent> found = repo.findbyId(created.id);
    assert found.isPresent() : "Created agent not found";
    assert "Agent X".equals(found.get().name);
    assert found.get().is_admin;
  }

  private static void testUpdateAndDelete() throws SQLException {
    AgentRepository repo = new AgentRepository();
    Agent created = repo.create("Agent Y", "agenty@example.com", "9876543210", false, "2026-06-14");
    Optional<Agent> updated = repo.update(created.id, "Agent Y", "agenty2@example.com", "9876543211", true, "2026-06-14");
    assert updated.isPresent() : "Update returned empty";
    assert "agenty2@example.com".equals(updated.get().email);
    assert updated.get().is_admin;

    boolean deleted = repo.delete(created.id);
    assert deleted : "Delete returned false";
    assert repo.findbyId(created.id).isEmpty() : "Deleted agent still found";
  }
}
