import db.AddressRepository;
import db.AgentRepository;
import db.ClientRepository;
import db.PropertyRepository;
import db.PropertyImageRepository;
import db.FavoriteRepository;
import db.ContractRepository;
import db.TransactionRepository;
import db.VisitRepository;
import db.Database;
import model.Address;
import model.Agent;
import model.Client;
import model.Property;
import model.PropertyImage;
import model.Favorite;
import model.Contract;
import model.Transaction;
import model.Visit;
import model.enums.ClientType;
import model.enums.PropertyType;
import model.enums.PropertyStatus;
import model.enums.ContractType;
import model.enums.TransactionStatus;

public class BackendTestHelper {

  public static void initDatabase() throws Exception {
    Database.init("jdbc:sqlite::memory:");
  }

  public static void closeDatabase() throws Exception {
    Database.close();
  }

  public static Agent createAgent() throws Exception {
    AgentRepository repo = new AgentRepository();
    return repo.create("Test Agent", "agent@test.local", "0123456789", true, "2026-06-14");
  }

  public static Address createAddress() throws Exception {
    AddressRepository repo = new AddressRepository();
    return repo.create("123 Test St", "Testville", 12345, "Testland");
  }

  public static Client createClient() throws Exception {
    ClientRepository repo = new ClientRepository();
    return repo.create("Test", "Client", "client@test.local", "0987654321", ClientType.BUYER);
  }

  public static Property createProperty(int agentId, int addressId) throws Exception {
    PropertyRepository repo = new PropertyRepository();
    return repo.create("Test Property", "A property for tests", 100000, 120.5f, 4,
        PropertyType.HOUSE, PropertyStatus.AVAILABLE, agentId, addressId, "2026-06-14");
  }

  public static PropertyImage createPropertyImage(int propertyId) throws Exception {
    PropertyImageRepository repo = new PropertyImageRepository();
    return repo.create(propertyId, "https://example.com/image.jpg", true, "2026-06-14");
  }

  public static Favorite createFavorite(int clientId, int propertyId) throws Exception {
    FavoriteRepository repo = new FavoriteRepository();
    return repo.create(clientId, propertyId, "2026-06-14");
  }

  public static Contract createContract(int propertyId, int clientId, int agentId) throws Exception {
    ContractRepository repo = new ContractRepository();
    return repo.create(propertyId, clientId, agentId, ContractType.SALE, "2026-06-14");
  }

  public static Transaction createTransaction(int contractId) throws Exception {
    TransactionRepository repo = new TransactionRepository();
    return repo.create(contractId, 50000, "2026-06-14", "bank_transfer", TransactionStatus.PAID);
  }

  public static Visit createVisit(int propertyId, int clientId, int agentId) throws Exception {
    VisitRepository repo = new VisitRepository();
    return repo.create(propertyId, clientId, agentId, "2026-06-14", "Great visit");
  }
}
