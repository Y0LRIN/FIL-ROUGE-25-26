import db.VisitRepository;
import db.Database;

import java.util.Optional;

public class VisitRepositoryTest {

  public static void main(String[] args) throws Exception {
    Database.init("jdbc:sqlite::memory:");
    try {
      testCreateAndFindById();
      testUpdateAndDelete();
      System.out.println("VisitRepositoryTest passed");
    } finally {
      Database.close();
    }
  }

  private static void testCreateAndFindById() throws Exception {
    db.AgentRepository agentRepo = new db.AgentRepository();
    db.AddressRepository addressRepo = new db.AddressRepository();
    db.PropertyRepository propertyRepo = new db.PropertyRepository();
    db.ClientRepository clientRepo = new db.ClientRepository();
    int agentId = agentRepo.create("Agent L", "l@example.com", "0123456756", false, "2026-06-14").id;
    int addressId = addressRepo.create("12 Street", "City", 120000, "Country").id;
    int propertyId = propertyRepo.create("Visit Prop", "Desc", 270000, 200.0f, 4,
        model.enums.PropertyType.APARTMENT, model.enums.PropertyStatus.AVAILABLE,
        agentId, addressId, "2026-06-14").id;
    int clientId = clientRepo.create("Visit", "Client", "visit@example.com", "7778889999", model.enums.ClientType.TENANT).id;

    VisitRepository repo = new VisitRepository();
    model.Visit created = repo.create(propertyId, clientId, agentId, "2026-06-14", "Nice visit");
    Optional<model.Visit> found = repo.findbyId(created.id);
    assert found.isPresent() : "Created visit not found";
    assert "Nice visit".equals(found.get().feedback);
  }

  private static void testUpdateAndDelete() throws Exception {
    db.AgentRepository agentRepo = new db.AgentRepository();
    db.AddressRepository addressRepo = new db.AddressRepository();
    db.PropertyRepository propertyRepo = new db.PropertyRepository();
    db.ClientRepository clientRepo = new db.ClientRepository();
    int agentId = agentRepo.create("Agent M", "m@example.com", "0123456767", false, "2026-06-14").id;
    int addressId = addressRepo.create("13 Street", "City", 130000, "Country").id;
    int propertyId = propertyRepo.create("Visit Prop 2", "Desc", 280000, 210.0f, 4,
        model.enums.PropertyType.HOUSE, model.enums.PropertyStatus.AVAILABLE,
        agentId, addressId, "2026-06-14").id;
    int clientId = clientRepo.create("Visit2", "Client", "visit2@example.com", "8889990000", model.enums.ClientType.BUYER).id;

    VisitRepository repo = new VisitRepository();
    model.Visit created = repo.create(propertyId, clientId, agentId, "2026-06-15", "Okay visit");
    Optional<model.Visit> updated = repo.update(created.id, propertyId, clientId, agentId, "2026-06-16", "Great visit");
    assert updated.isPresent() : "Update returned empty";
    assert "Great visit".equals(updated.get().feedback);

    boolean deleted = repo.delete(created.id);
    assert deleted : "Delete returned false";
    assert repo.findbyId(created.id).isEmpty() : "Deleted visit still found";
  }
}
