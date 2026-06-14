import db.PropertyImageRepository;
import db.Database;

import java.util.Optional;

public class PropertyImageRepositoryTest {

  public static void main(String[] args) throws Exception {
    Database.init("jdbc:sqlite::memory:");
    try {
      testCreateAndFindById();
      testUpdateAndDelete();
      System.out.println("PropertyImageRepositoryTest passed");
    } finally {
      Database.close();
    }
  }

  private static void testCreateAndFindById() throws Exception {
    db.AgentRepository agentRepo = new db.AgentRepository();
    db.AddressRepository addressRepo = new db.AddressRepository();
    db.PropertyRepository propertyRepo = new db.PropertyRepository();
    int agentId = agentRepo.create("Agent C", "c@example.com", "0123000000", false, "2026-06-14").id;
    int addressId = addressRepo.create("3 Street", "Village", 30000, "Country").id;
    int propertyId = propertyRepo.create("Image Prop", "Desc", 180000, 120.0f, 4,
        model.enums.PropertyType.COMMERCIAL, model.enums.PropertyStatus.AVAILABLE,
        agentId, addressId, "2026-06-14").id;

    PropertyImageRepository repo = new PropertyImageRepository();
    model.PropertyImage created = repo.create(propertyId, "https://example.com/1.jpg", true, "2026-06-14");
    Optional<model.PropertyImage> found = repo.findbyId(created.id);
    assert found.isPresent() : "Created property image not found";
    assert found.get().is_main;
  }

  private static void testUpdateAndDelete() throws Exception {
    db.AgentRepository agentRepo = new db.AgentRepository();
    db.AddressRepository addressRepo = new db.AddressRepository();
    db.PropertyRepository propertyRepo = new db.PropertyRepository();
    int agentId = agentRepo.create("Agent D", "d@example.com", "0123000001", false, "2026-06-14").id;
    int addressId = addressRepo.create("4 Street", "Village", 40000, "Country").id;
    int propertyId = propertyRepo.create("Image Prop 2", "Desc", 190000, 130.0f, 4,
        model.enums.PropertyType.HOUSE, model.enums.PropertyStatus.AVAILABLE,
        agentId, addressId, "2026-06-14").id;

    PropertyImageRepository repo = new PropertyImageRepository();
    model.PropertyImage created = repo.create(propertyId, "https://example.com/2.jpg", false, "2026-06-14");
    Optional<model.PropertyImage> updated = repo.update(created.id, propertyId, "https://example.com/2-updated.jpg", true, "2026-06-14");
    assert updated.isPresent() : "Update returned empty";
    assert updated.get().is_main;

    boolean deleted = repo.delete(created.id);
    assert deleted : "Delete returned false";
    assert repo.findbyId(created.id).isEmpty() : "Deleted property image still found";
  }
}
