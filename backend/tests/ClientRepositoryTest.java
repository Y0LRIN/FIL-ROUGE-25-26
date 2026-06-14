import db.ClientRepository;
import db.Database;
import model.Client;
import model.enums.ClientType;

import java.sql.SQLException;
import java.util.Optional;
import java.util.List;

public class ClientRepositoryTest {

  public static void main(String[] args) throws Exception {
    Database.init("jdbc:sqlite::memory:");
    try {
      testCreateAndFindById();
      testUpdateAndDelete();
      System.out.println("ClientRepositoryTest passed");
    } finally {
      Database.close();
    }
  }

  private static void testCreateAndFindById() throws SQLException {
    ClientRepository repo = new ClientRepository();
    Client created = repo.create("Alice", "Martin", "alice@example.com", "1234567890", ClientType.BUYER);
    Optional<Client> found = repo.findbyId(created.id);
    assert found.isPresent() : "Created client not found";
    assert "Alice".equals(found.get().first_name);
    assert found.get().type == ClientType.BUYER;
  }

  private static void testUpdateAndDelete() throws SQLException {
    ClientRepository repo = new ClientRepository();
    Client created = repo.create("Bob", "Dupont", "bob@example.com", "0987654321", ClientType.SELLER);
    Optional<Client> updated = repo.update(created.id, "Bob", "Dupont", "bob2@example.com", "0987654322", ClientType.LANDLORD);
    assert updated.isPresent() : "Update returned empty";
    assert "bob2@example.com".equals(updated.get().email);
    assert updated.get().type == ClientType.LANDLORD;

    boolean deleted = repo.delete(created.id);
    assert deleted : "Delete returned false";
    assert repo.findbyId(created.id).isEmpty() : "Deleted client still found";
  }
}
