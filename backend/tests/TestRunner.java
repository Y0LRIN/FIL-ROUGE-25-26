public class TestRunner {
  public static void main(String[] args) throws Exception {
    ClientRepositoryTest.main(args);
    AgentRepositoryTest.main(args);
    System.out.println("All tests passed");
  }
}
