import db.PropertyRepository;
import db.Database;
import model.Property;
import model.enums.PropertyType;
import model.enums.PropertyStatus;

import java.util.Optional;

public class PropertyRepositoryTest {

  public static void main(String[] args) throws Exception {
    Database.init("jdbc:sqlite::memory:");
    try {
      testCreateAndFindById();
      testUpdateAndDelete();
      System.out.println("PropertyRepositoryTest passed");
    } finally {
      Database.close();
    }
  }

  private static void testCreateAndFindById() throws Exception {
    db.AgentRepository agentRepo = new db.AgentRepository();
    db.AddressRepository addressRepo = new db.AddressRepository();
    int agentId = agentRepo.create("Agent A", "a@example.com", "0123456789", false, "2026-06-14").id;
    int addressId = addressRepo.create("1 Street", "City", 10000, "Country").id;

    PropertyRepository repo = new PropertyRepository();
    Property created = repo.create("Test Prop", "Description", 150000, 90.0f, 3,
        PropertyType.APARTMENT, PropertyStatus.AVAILABLE, agentId, addressId, "2026-06-14");
    Optional<Property> found = repo.findbyId(created.id);
    assert found.isPresent() : "Created property not found";
    assert "Test Prop".equals(found.get().title);
  }

  private static void testUpdateAndDelete() throws Exception {
    db.AgentRepository agentRepo = new db.AgentRepository();
    db.AddressRepository addressRepo = new db.AddressRepository();
    int agentId = agentRepo.create("Agent B", "b@example.com", "9876543210", false, "2026-06-14").id;
    int addressId = addressRepo.create("2 Street", "Town", 20000, "Country").id;

    PropertyRepository repo = new PropertyRepository();
    Property created = repo.create("Test Prop 2", "Description", 160000, 100.0f, 4,
        PropertyType.HOUSE, PropertyStatus.AVAILABLE, agentId, addressId, "2026-06-14");
    Optional<Property> updated = repo.update(created.id, "Updated Prop", "Updated", 170000,
        110.0f, 5, PropertyType.HOUSE, PropertyStatus.PENDING, agentId, addressId, "2026-06-14");
    assert updated.isPresent() : "Update returned empty";
    assert "Updated Prop".equals(updated.get().title);

    boolean deleted = repo.delete(created.id);
    assert deleted : "Delete returned false";
    assert repo.findbyId(created.id).isEmpty() : "Deleted property still found";
  }
}
