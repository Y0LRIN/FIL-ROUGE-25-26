import db.ContractRepository;
import db.Database;

import java.util.Optional;

public class ContractRepositoryTest {

  public static void main(String[] args) throws Exception {
    Database.init("jdbc:sqlite::memory:");
    try {
      testCreateAndFindById();
      testUpdateAndDelete();
      System.out.println("ContractRepositoryTest passed");
    } finally {
      Database.close();
    }
  }

  private static void testCreateAndFindById() throws Exception {
    db.AgentRepository agentRepo = new db.AgentRepository();
    db.AddressRepository addressRepo = new db.AddressRepository();
    db.PropertyRepository propertyRepo = new db.PropertyRepository();
    db.ClientRepository clientRepo = new db.ClientRepository();
    int agentId = agentRepo.create("Agent H", "h@example.com", "0123456712", false, "2026-06-14").id;
    int addressId = addressRepo.create("8 Street", "City", 80000, "Country").id;
    int propertyId = propertyRepo.create("Contract Prop", "Desc", 230000, 160.0f, 4,
        model.enums.PropertyType.HOUSE, model.enums.PropertyStatus.AVAILABLE,
        agentId, addressId, "2026-06-14").id;
    int clientId = clientRepo.create("Contract", "Client", "contract@example.com", "3334445555", model.enums.ClientType.SELLER).id;

    ContractRepository repo = new ContractRepository();
    model.Contract created = repo.create(propertyId, clientId, agentId, model.enums.ContractType.SALE, "2026-06-14");
    Optional<model.Contract> found = repo.findbyId(created.id);
    assert found.isPresent() : "Created contract not found";
    assert found.get().type == model.enums.ContractType.SALE;
  }

  private static void testUpdateAndDelete() throws Exception {
    db.AgentRepository agentRepo = new db.AgentRepository();
    db.AddressRepository addressRepo = new db.AddressRepository();
    db.PropertyRepository propertyRepo = new db.PropertyRepository();
    db.ClientRepository clientRepo = new db.ClientRepository();
    int agentId = agentRepo.create("Agent I", "i@example.com", "0123456723", false, "2026-06-14").id;
    int addressId = addressRepo.create("9 Street", "City", 90000, "Country").id;
    int propertyId = propertyRepo.create("Contract Prop 2", "Desc", 240000, 170.0f, 4,
        model.enums.PropertyType.COMMERCIAL, model.enums.PropertyStatus.AVAILABLE,
        agentId, addressId, "2026-06-14").id;
    int clientId = clientRepo.create("Contract2", "Client", "contract2@example.com", "4445556666", model.enums.ClientType.LANDLORD).id;

    ContractRepository repo = new ContractRepository();
    model.Contract created = repo.create(propertyId, clientId, agentId, model.enums.ContractType.RENTAL, "2026-06-14");
    Optional<model.Contract> updated = repo.update(created.id, propertyId, clientId, agentId, model.enums.ContractType.SALE, "2026-06-15");
    assert updated.isPresent() : "Update returned empty";
    assert updated.get().type == model.enums.ContractType.SALE;

    boolean deleted = repo.delete(created.id);
    assert deleted : "Delete returned false";
    assert repo.findbyId(created.id).isEmpty() : "Deleted contract still found";
  }
}
