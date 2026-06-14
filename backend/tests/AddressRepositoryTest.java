import db.AddressRepository;
import db.Database;
import model.Address;

import java.util.Optional;

public class AddressRepositoryTest {

  public static void main(String[] args) throws Exception {
    Database.init("jdbc:sqlite::memory:");
    try {
      testCreateAndFindById();
      testUpdateAndDelete();
      System.out.println("AddressRepositoryTest passed");
    } finally {
      Database.close();
    }
  }

  private static void testCreateAndFindById() throws Exception {
    AddressRepository repo = new AddressRepository();
    Address created = repo.create("123 Test St", "Testville", 12345, "Testland");
    Optional<Address> found = repo.findbyId(created.id);
    assert found.isPresent() : "Created address not found";
    assert "Testville".equals(found.get().city);
    assert found.get().postal_code == 12345;
  }

  private static void testUpdateAndDelete() throws Exception {
    AddressRepository repo = new AddressRepository();
    Address created = repo.create("456 Main St", "Example City", 67890, "ExampleLand");
    Optional<Address> updated = repo.update(created.id, "456 Main St", "Example City", 67891, "ExampleLand");
    assert updated.isPresent() : "Update returned empty";
    assert updated.get().postal_code == 67891;

    boolean deleted = repo.delete(created.id);
    assert deleted : "Delete returned false";
    assert repo.findbyId(created.id).isEmpty() : "Deleted address still found";
  }
}
