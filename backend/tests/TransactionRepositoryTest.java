import db.TransactionRepository;
import db.Database;

import java.util.Optional;

public class TransactionRepositoryTest {

  public static void main(String[] args) throws Exception {
    Database.init("jdbc:sqlite::memory:");
    try {
      testCreateAndFindById();
      testUpdateAndDelete();
      System.out.println("TransactionRepositoryTest passed");
    } finally {
      Database.close();
    }
  }

  private static void testCreateAndFindById() throws Exception {
    db.AgentRepository agentRepo = new db.AgentRepository();
    db.AddressRepository addressRepo = new db.AddressRepository();
    db.PropertyRepository propertyRepo = new db.PropertyRepository();
    db.ClientRepository clientRepo = new db.ClientRepository();
    db.ContractRepository contractRepo = new db.ContractRepository();
    int agentId = agentRepo.create("Agent J", "j@example.com", "0123456734", false, "2026-06-14").id;
    int addressId = addressRepo.create("10 Street", "City", 100000, "Country").id;
    int propertyId = propertyRepo.create("Transaction Prop", "Desc", 250000, 180.0f, 4,
        model.enums.PropertyType.COMMERCIAL, model.enums.PropertyStatus.AVAILABLE,
        agentId, addressId, "2026-06-14").id;
    int clientId = clientRepo.create("Transaction", "Client", "transaction@example.com", "5556667777", model.enums.ClientType.BUYER).id;
    int contractId = contractRepo.create(propertyId, clientId, agentId, model.enums.ContractType.SALE, "2026-06-14").id;

    TransactionRepository repo = new TransactionRepository();
    model.Transaction created = repo.create(contractId, 123456, "2026-06-14", "cash", model.enums.TransactionStatus.PAID);
    Optional<model.Transaction> found = repo.findbyId(created.id);
    assert found.isPresent() : "Created transaction not found";
    assert found.get().amount == 123456;
  }

  private static void testUpdateAndDelete() throws Exception {
    db.AgentRepository agentRepo = new db.AgentRepository();
    db.AddressRepository addressRepo = new db.AddressRepository();
    db.PropertyRepository propertyRepo = new db.PropertyRepository();
    db.ClientRepository clientRepo = new db.ClientRepository();
    db.ContractRepository contractRepo = new db.ContractRepository();
    int agentId = agentRepo.create("Agent K", "k@example.com", "0123456745", false, "2026-06-14").id;
    int addressId = addressRepo.create("11 Street", "City", 110000, "Country").id;
    int propertyId = propertyRepo.create("Transaction Prop 2", "Desc", 260000, 190.0f, 4,
        model.enums.PropertyType.HOUSE, model.enums.PropertyStatus.AVAILABLE,
        agentId, addressId, "2026-06-14").id;
    int clientId = clientRepo.create("Transaction2", "Client", "transaction2@example.com", "6667778888", model.enums.ClientType.SELLER).id;
    int contractId = contractRepo.create(propertyId, clientId, agentId, model.enums.ContractType.RENTAL, "2026-06-14").id;

    TransactionRepository repo = new TransactionRepository();
    model.Transaction created = repo.create(contractId, 654321, "2026-06-15", "card", model.enums.TransactionStatus.PENDING);
    Optional<model.Transaction> updated = repo.update(created.id, contractId, 654321, "2026-06-16", "card", model.enums.TransactionStatus.PAID);
    assert updated.isPresent() : "Update returned empty";
    assert updated.get().payment_date.equals("2026-06-16");

    boolean deleted = repo.delete(created.id);
    assert deleted : "Delete returned false";
    assert repo.findbyId(created.id).isEmpty() : "Deleted transaction still found";
  }
}
