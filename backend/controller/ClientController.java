package controller;

import db.ClientRepository;
import model.Client;
import model.enums.ClientType;
import util.HttpUtils;
import util.Json;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ClientController {

  private final ClientRepository repo = new ClientRepository();

  public void handle(HttpExchange ex) throws IOException {
    int id = HttpUtils.segmentInt(ex, 3);

    try {
      switch (ex.getRequestMethod()) {
        case "GET" -> {
          if (id > 0) {
            getOne(ex, id);
          } else {
            getAll(ex);
          }
        }
        case "POST" -> create(ex);
        case "PUT" -> update(ex, id);
        case "DELETE" -> delete(ex, id);
        default -> HttpUtils.sendJson(ex, 405, Json.error("Unauthorized method"));
      }
    } catch (Exception e) {
      HttpUtils.sendJson(ex, 500, Json.error(e.getMessage()));
    }
  }

  // HANDLERS

  private void getAll(HttpExchange ex) throws Exception {
    List<Client> clients = repo.findAll();
    List<Map<String, Object>> list = clients.stream().map(this::toMap).toList();
    HttpUtils.sendJson(ex, 200, Json.toJson(list));
  }

  private void getOne(HttpExchange ex, int id) throws Exception {
    Optional<Client> client = repo.findbyId(id);
    if (client.isEmpty()) {
      HttpUtils.sendJson(ex, 404, Json.error("Unknown Client"));
      return;
    }
    HttpUtils.sendJson(ex, 200, Json.toJson(toMap(client.get())));
  }

  private void create(HttpExchange ex) throws Exception {
    Map<String, String> body = Json.parse(HttpUtils.readBody(ex));
    String first_name = body.get("first_name");
    String last_name = body.get("last_name");
    String email = body.get("email");
    String phone = body.get("phone");
    String typeStr = body.get("type");
    if (first_name == null || last_name == null || email == null || phone == null || typeStr == null) {
      HttpUtils.sendJson(ex, 400, Json.error("All fields required (first_name/last_name/email/phone/type)"));
      return;
    }
    ClientType type = ClientType.valueOf(typeStr);
    Client created = repo.create(first_name, last_name, email, phone, type);
    HttpUtils.sendJson(ex, 201, Json.toJson(toMap(created)));
  }

  private void update(HttpExchange ex, int id) throws Exception {
    if (id <= 0) {
      HttpUtils.sendJson(ex, 400, Json.error("Invalid ID"));
      return;
    }

    Map<String, String> body = Json.parse(HttpUtils.readBody(ex));
    String first_name = body.get("first_name");
    String last_name = body.get("last_name");
    String email = body.get("email");
    String phone = body.get("phone");
    String typeStr = body.get("type");
    if (first_name == null || last_name == null || email == null || phone == null || typeStr == null) {
      HttpUtils.sendJson(ex, 400, Json.error("All fields required (first_name/last_name/email/phone/type)"));
      return;
    }
    ClientType type = ClientType.valueOf(typeStr);
    Optional<Client> updated = repo.update(id, first_name, last_name, email, phone, type);
    if (updated.isEmpty()) {
      HttpUtils.sendJson(ex, 404, Json.error("Client Unknown"));
      return;
    }
    HttpUtils.sendJson(ex, 200, Json.toJson(toMap(updated.get())));
  }

  private void delete(HttpExchange ex, int id) throws Exception {
    if (id <= 0) {
      HttpUtils.sendJson(ex, 400, Json.error("Invalid ID"));
      return;
    }
    if (!repo.delete(id)) {
      HttpUtils.sendJson(ex, 404, Json.error("Client not found"));
      return;
    }
    HttpUtils.sendNoContent(ex);
  }

  private Map<String, Object> toMap(Client c) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", c.id);
    m.put("first_name", c.first_name);
    m.put("last_name", c.last_name);
    m.put("email", c.email);
    m.put("phone", c.phone);
    m.put("type", c.type);
    return m;
  }
}
