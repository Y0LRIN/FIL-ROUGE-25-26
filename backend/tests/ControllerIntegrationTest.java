import com.sun.net.httpserver.HttpServer;
import db.Database;
import util.Json;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControllerIntegrationTest {

  private static final HttpClient client = HttpClient.newHttpClient();
  private static String baseUrl;

  public static void main(String[] args) throws Exception {
    HttpServer server = null;
    try {
      Database.init("jdbc:sqlite::memory:");
      server = Main.createServer(0);
      server.start();
      InetSocketAddress address = (InetSocketAddress) server.getAddress();
      baseUrl = "http://localhost:" + address.getPort();

      testOptionsSupport();
      testClientController();
      testAgentController();
      testAddressController();
      testPropertyController();
      testPropertyImageController();
      testFavoriteController();
      testContractAndTransactionController();
      testVisitController();

      System.out.println("ControllerIntegrationTest passed");
    } finally {
      if (server != null) {
        server.stop(0);
      }
      Database.close();
    }
  }

  private static void testOptionsSupport() throws Exception {
    HttpResponse<String> response = request("OPTIONS", "/api/clients", null);
    assert response.statusCode() == 204 : "OPTIONS should return 204";
    assert response.headers().firstValue("access-control-allow-origin").orElse("null").equals("*");
  }

  private static void testClientController() throws Exception {
    Map<String, Object> body = new HashMap<>();
    body.put("first_name", "Alice");
    body.put("last_name", "Martin");
    body.put("email", "alice@example.com");
    body.put("phone", "1234567890");
    body.put("type", "BUYER");

    HttpResponse<String> create = post("/api/clients", Json.toJson(body));
    assert create.statusCode() == 201 : "Client POST should return 201";
    Map<String, String> created = Json.parse(create.body());
    int clientId = Integer.parseInt(created.get("id"));
    assert "Alice".equals(created.get("first_name"));

    HttpResponse<String> getOne = get("/api/clients/" + clientId);
    assert getOne.statusCode() == 200 : "Client GET should return 200";
    Map<String, String> fetched = Json.parse(getOne.body());
    assert String.valueOf(clientId).equals(fetched.get("id"));

    body.put("email", "alice2@example.com");
    HttpResponse<String> update = put("/api/clients/" + clientId, Json.toJson(body));
    assert update.statusCode() == 200 : "Client PUT should return 200";
    Map<String, String> updated = Json.parse(update.body());
    assert "alice2@example.com".equals(updated.get("email"));

    HttpResponse<String> delete = delete("/api/clients/" + clientId);
    assert delete.statusCode() == 204 : "Client DELETE should return 204";

    HttpResponse<String> missing = get("/api/clients/" + clientId);
    assert missing.statusCode() == 404 : "Deleted client should return 404";
  }

  private static void testAgentController() throws Exception {
    Map<String, Object> body = new HashMap<>();
    body.put("name", "Agent X");
    body.put("email", "agentx@example.com");
    body.put("phone", "0123456789");
    body.put("is_admin", true);
    body.put("created_at", "2026-06-14");

    HttpResponse<String> create = post("/api/agents", Json.toJson(body));
    assert create.statusCode() == 201 : "Agent POST should return 201";
    Map<String, String> created = Json.parse(create.body());
    int agentId = Integer.parseInt(created.get("id"));
    assert "Agent X".equals(created.get("name"));

    HttpResponse<String> getOne = get("/api/agents/" + agentId);
    assert getOne.statusCode() == 200 : "Agent GET should return 200";

    body.put("email", "agentx2@example.com");
    HttpResponse<String> update = put("/api/agents/" + agentId, Json.toJson(body));
    assert update.statusCode() == 200 : "Agent PUT should return 200";
    Map<String, String> updated = Json.parse(update.body());
    assert "agentx2@example.com".equals(updated.get("email"));

    HttpResponse<String> delete = delete("/api/agents/" + agentId);
    assert delete.statusCode() == 204 : "Agent DELETE should return 204";
  }

  private static void testAddressController() throws Exception {
    Map<String, Object> body = new HashMap<>();
    body.put("street", "123 Test St");
    body.put("city", "Testville");
    body.put("postal_code", 12345);
    body.put("country", "Testland");

    HttpResponse<String> create = post("/api/addresses", Json.toJson(body));
    assert create.statusCode() == 201 : "Address POST should return 201";
    Map<String, String> created = Json.parse(create.body());
    int addressId = Integer.parseInt(created.get("id"));
    assert "123 Test St".equals(created.get("street"));

    body.put("city", "Newville");
    HttpResponse<String> update = put("/api/addresses/" + addressId, Json.toJson(body));
    assert update.statusCode() == 200 : "Address PUT should return 200";
    Map<String, String> updated = Json.parse(update.body());
    assert "Newville".equals(updated.get("city"));

    HttpResponse<String> delete = delete("/api/addresses/" + addressId);
    assert delete.statusCode() == 204 : "Address DELETE should return 204";
  }

  private static void testPropertyController() throws Exception {
    int agentId = createAgent();
    int addressId = createAddress();

    Map<String, Object> body = new HashMap<>();
    body.put("title", "Test Property");
    body.put("description", "A property for tests");
    body.put("price", 100000);
    body.put("surface", 120.5f);
    body.put("rooms", 4);
    body.put("type", "HOUSE");
    body.put("status", "AVAILABLE");
    body.put("agent_id", agentId);
    body.put("address_id", addressId);
    body.put("created_at", "2026-06-14");

    HttpResponse<String> create = post("/api/properties", Json.toJson(body));
    assert create.statusCode() == 201 : "Property POST should return 201";
    Map<String, String> created = Json.parse(create.body());
    int propertyId = Integer.parseInt(created.get("id"));
    assert "Test Property".equals(created.get("title"));

    body.put("price", 120000);
    HttpResponse<String> update = put("/api/properties/" + propertyId, Json.toJson(body));
    assert update.statusCode() == 200 : "Property PUT should return 200";
    Map<String, String> updated = Json.parse(update.body());
    assert "120000".equals(updated.get("price"));

    HttpResponse<String> delete = delete("/api/properties/" + propertyId);
    assert delete.statusCode() == 204 : "Property DELETE should return 204";
  }

  private static void testPropertyImageController() throws Exception {
    int agentId = createAgent();
    int addressId = createAddress();
    int propertyId = createProperty(agentId, addressId);

    Map<String, Object> body = new HashMap<>();
    body.put("property_id", propertyId);
    body.put("image_url", "https://example.com/image.jpg");
    body.put("is_main", true);
    body.put("created_at", "2026-06-14");

    HttpResponse<String> create = post("/api/property_images", Json.toJson(body));
    assert create.statusCode() == 201 : "PropertyImage POST should return 201";
    Map<String, String> created = Json.parse(create.body());
    int imageId = Integer.parseInt(created.get("id"));

    body.put("image_url", "https://example.com/image2.jpg");
    HttpResponse<String> update = put("/api/property_images/" + imageId, Json.toJson(body));
    assert update.statusCode() == 200 : "PropertyImage PUT should return 200";

    HttpResponse<String> delete = delete("/api/property_images/" + imageId);
    assert delete.statusCode() == 204 : "PropertyImage DELETE should return 204";
  }

  private static void testFavoriteController() throws Exception {
    int agentId = createAgent();
    int addressId = createAddress();
    int propertyId = createProperty(agentId, addressId);
    int clientId = createClient();

    Map<String, Object> body = new HashMap<>();
    body.put("client_id", clientId);
    body.put("property_id", propertyId);
    body.put("created_at", "2026-06-14");

    HttpResponse<String> create = post("/api/favorites", Json.toJson(body));
    assert create.statusCode() == 201 : "Favorite POST should return 201";
    Map<String, String> created = Json.parse(create.body());
    int favoriteId = Integer.parseInt(created.get("id"));

    HttpResponse<String> getOne = get("/api/favorites/" + favoriteId);
    assert getOne.statusCode() == 200 : "Favorite GET should return 200";

    HttpResponse<String> delete = delete("/api/favorites/" + favoriteId);
    assert delete.statusCode() == 204 : "Favorite DELETE should return 204";
  }

  private static void testContractAndTransactionController() throws Exception {
    int agentId = createAgent();
    int addressId = createAddress();
    int propertyId = createProperty(agentId, addressId);
    int clientId = createClient();

    Map<String, Object> contractBody = new HashMap<>();
    contractBody.put("property_id", propertyId);
    contractBody.put("client_id", clientId);
    contractBody.put("agent_id", agentId);
    contractBody.put("type", "SALE");
    contractBody.put("signed_at", "2026-06-14");

    HttpResponse<String> contractCreate = post("/api/contracts", Json.toJson(contractBody));
    assert contractCreate.statusCode() == 201 : "Contract POST should return 201";
    Map<String, String> contractCreated = Json.parse(contractCreate.body());
    int contractId = Integer.parseInt(contractCreated.get("id"));

    contractBody.put("type", "RENTAL");
    HttpResponse<String> contractUpdate = put("/api/contracts/" + contractId, Json.toJson(contractBody));
    assert contractUpdate.statusCode() == 200 : "Contract PUT should return 200";

    Map<String, Object> transactionBody = new HashMap<>();
    transactionBody.put("contract_id", contractId);
    transactionBody.put("amount", 50000);
    transactionBody.put("payment_date", "2026-06-14");
    transactionBody.put("payment_method", "bank_transfer");
    transactionBody.put("status", "PAID");

    HttpResponse<String> transactionCreate = post("/api/transactions", Json.toJson(transactionBody));
    assert transactionCreate.statusCode() == 201 : "Transaction POST should return 201";
    Map<String, String> transactionCreated = Json.parse(transactionCreate.body());
    int transactionId = Integer.parseInt(transactionCreated.get("id"));

    transactionBody.put("amount", 60000);
    HttpResponse<String> transactionUpdate = put("/api/transactions/" + transactionId, Json.toJson(transactionBody));
    assert transactionUpdate.statusCode() == 200 : "Transaction PUT should return 200";

    HttpResponse<String> transactionDelete = delete("/api/transactions/" + transactionId);
    assert transactionDelete.statusCode() == 204 : "Transaction DELETE should return 204";
  }

  private static void testVisitController() throws Exception {
    int agentId = createAgent();
    int addressId = createAddress();
    int propertyId = createProperty(agentId, addressId);
    int clientId = createClient();

    Map<String, Object> body = new HashMap<>();
    body.put("property_id", propertyId);
    body.put("client_id", clientId);
    body.put("agent_id", agentId);
    body.put("visit_date", "2026-06-14");
    body.put("feedback", "Great visit");

    HttpResponse<String> create = post("/api/visits", Json.toJson(body));
    assert create.statusCode() == 201 : "Visit POST should return 201";
    Map<String, String> created = Json.parse(create.body());
    int visitId = Integer.parseInt(created.get("id"));

    HttpResponse<String> update = put("/api/visits/" + visitId, Json.toJson(body));
    assert update.statusCode() == 200 : "Visit PUT should return 200";

    HttpResponse<String> delete = delete("/api/visits/" + visitId);
    assert delete.statusCode() == 204 : "Visit DELETE should return 204";
  }

  private static int createAgent() throws Exception {
    Map<String, Object> body = new HashMap<>();
    body.put("name", "Agent Helper");
    body.put("email", "helper@example.com");
    body.put("phone", "0123456789");
    body.put("is_admin", false);
    body.put("created_at", "2026-06-14");
    Map<String, String> created = Json.parse(post("/api/agents", Json.toJson(body)).body());
    return Integer.parseInt(created.get("id"));
  }

  private static int createAddress() throws Exception {
    Map<String, Object> body = new HashMap<>();
    body.put("street", "456 Helper Rd");
    body.put("city", "Helperton");
    body.put("postal_code", 54321);
    body.put("country", "Helperland");
    Map<String, String> created = Json.parse(post("/api/addresses", Json.toJson(body)).body());
    return Integer.parseInt(created.get("id"));
  }

  private static int createClient() throws Exception {
    Map<String, Object> body = new HashMap<>();
    body.put("first_name", "Helper");
    body.put("last_name", "Client");
    body.put("email", "helperclient@example.com");
    body.put("phone", "0987654321");
    body.put("type", "SELLER");
    Map<String, String> created = Json.parse(post("/api/clients", Json.toJson(body)).body());
    return Integer.parseInt(created.get("id"));
  }

  private static int createProperty(int agentId, int addressId) throws Exception {
    Map<String, Object> body = new HashMap<>();
    body.put("title", "Helper Property");
    body.put("description", "Property for helper tests");
    body.put("price", 90000);
    body.put("surface", 100.0f);
    body.put("rooms", 3);
    body.put("type", "APARTMENT");
    body.put("status", "AVAILABLE");
    body.put("agent_id", agentId);
    body.put("address_id", addressId);
    body.put("created_at", "2026-06-14");
    Map<String, String> created = Json.parse(post("/api/properties", Json.toJson(body)).body());
    return Integer.parseInt(created.get("id"));
  }

  private static HttpResponse<String> get(String path) throws Exception {
    return request("GET", path, null);
  }

  private static HttpResponse<String> post(String path, String json) throws Exception {
    return request("POST", path, json);
  }

  private static HttpResponse<String> put(String path, String json) throws Exception {
    return request("PUT", path, json);
  }

  private static HttpResponse<String> delete(String path) throws Exception {
    return request("DELETE", path, null);
  }

  private static HttpResponse<String> request(String method, String path, String json) throws Exception {
    HttpRequest.Builder builder = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + path))
        .header("Content-Type", "application/json");

    switch (method) {
      case "POST" -> builder.POST(HttpRequest.BodyPublishers.ofString(json != null ? json : ""));
      case "PUT" -> builder.PUT(HttpRequest.BodyPublishers.ofString(json != null ? json : ""));
      case "DELETE" -> builder.DELETE();
      case "OPTIONS" -> builder.method("OPTIONS", HttpRequest.BodyPublishers.noBody());
      default -> builder.GET();
    }

    return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
  }
}
