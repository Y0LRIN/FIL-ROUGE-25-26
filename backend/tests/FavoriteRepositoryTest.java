import db.FavoriteRepository;
import db.Database;

import java.util.Optional;

public class FavoriteRepositoryTest {

  public static void main(String[] args) throws Exception {
    Database.init("jdbc:sqlite::memory:");
    try {
      testCreateAndFindById();
      testUpdateAndDelete();
      System.out.println("FavoriteRepositoryTest passed");
    } finally {
      Database.close();
    }
  }

  private static void testCreateAndFindById() throws Exception {
    db.AgentRepository agentRepo = new db.AgentRepository();
    db.AddressRepository addressRepo = new db.AddressRepository();
    db.PropertyRepository propertyRepo = new db.PropertyRepository();
    db.ClientRepository clientRepo = new db.ClientRepository();
    int agentId = agentRepo.create("Agent F", "f@example.com", "0123456789", false, "2026-06-14").id;
    int addressId = addressRepo.create("6 Street", "City", 60000, "Country").id;
    int propertyId = propertyRepo.create("Fav Prop", "Desc", 210000, 140.0f, 4,
        model.enums.PropertyType.HOUSE, model.enums.PropertyStatus.AVAILABLE,
        agentId, addressId, "2026-06-14").id;
    int clientId = clientRepo.create("Fav", "Client", "fav@example.com", "1112223333", model.enums.ClientType.TENANT).id;

    FavoriteRepository repo = new FavoriteRepository();
    model.Favorite created = repo.create(clientId, propertyId, "2026-06-14");
    Optional<model.Favorite> found = repo.findbyId(created.id);
    assert found.isPresent() : "Created favorite not found";
    assert found.get().client_id == clientId;
  }

  private static void testUpdateAndDelete() throws Exception {
    db.AgentRepository agentRepo = new db.AgentRepository();
    db.AddressRepository addressRepo = new db.AddressRepository();
    db.PropertyRepository propertyRepo = new db.PropertyRepository();
    db.ClientRepository clientRepo = new db.ClientRepository();
    int agentId = agentRepo.create("Agent G", "g@example.com", "0123456790", false, "2026-06-14").id;
    int addressId = addressRepo.create("7 Street", "City", 70000, "Country").id;
    int propertyId = propertyRepo.create("Fav Prop 2", "Desc", 220000, 150.0f, 4,
        model.enums.PropertyType.APARTMENT, model.enums.PropertyStatus.AVAILABLE,
        agentId, addressId, "2026-06-14").id;
    int clientId = clientRepo.create("Fav2", "Client", "fav2@example.com", "2223334444", model.enums.ClientType.BUYER).id;

    FavoriteRepository repo = new FavoriteRepository();
    model.Favorite created = repo.create(clientId, propertyId, "2026-06-14");
    Optional<model.Favorite> updated = repo.update(created.id, clientId, propertyId, "2026-06-15");
    assert updated.isPresent() : "Update returned empty";
    assert "2026-06-15".equals(updated.get().created_at);

    boolean deleted = repo.delete(created.id);
    assert deleted : "Delete returned false";
    assert repo.findbyId(created.id).isEmpty() : "Deleted favorite still found";
  }
}
