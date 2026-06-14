public class FullTestRunner {
  public static void main(String[] args) throws Exception {
    ClientRepositoryTest.main(args);
    AgentRepositoryTest.main(args);
    AddressRepositoryTest.main(args);
    PropertyRepositoryTest.main(args);
    PropertyImageRepositoryTest.main(args);
    FavoriteRepositoryTest.main(args);
    ContractRepositoryTest.main(args);
    TransactionRepositoryTest.main(args);
    VisitRepositoryTest.main(args);
    ControllerIntegrationTest.main(args);
    System.out.println("All backend tests passed");
  }
}
