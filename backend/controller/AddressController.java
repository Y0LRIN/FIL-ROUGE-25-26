package controller;

import db.AddressRepository;
import model.Address;
import util.HttpUtils;
import util.Json;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AddressController {

  private final AddressRepository repo = new AddressRepository();

  public void handle(HttpExchange ex) throws IOException {
    int id = HttpUtils.segmentInt(ex, 3);

    try {
      switch (ex.getRequestMethod()) {
        case "GET" -> {
          if (id > 0)
            getOne(ex, id);
          else
            getAll(ex);
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
    List<Address> addresses = repo.findAll();
    List<Map<String, Object>> list = addresses.stream().map(this::toMap).toList();
    HttpUtils.sendJson(ex, 200, Json.toJson(list));
  }

  private void getOne(HttpExchange ex, int id) throws Exception {
    Optional<Address> address = repo.findbyId(id);
    if (address.isEmpty()) {
      HttpUtils.sendJson(ex, 404, "Unknown address");
      return;
    }
    HttpUtils.sendJson(ex, 200, Json.toJson(toMap(address.get())));
  }

  private void create(HttpExchange ex) throws Exception {
    Map<String, String> body = Json.parse(HttpUtils.readBody(ex));
    String street = body.get("street");
    String city = body.get("city");
    String postal_codeStr = body.get("postal_code");
    String country = body.get("country");
    if (street == null || city == null || postal_codeStr == null || country == null) {
      HttpUtils.sendJson(ex, 400, Json.error("All fields required (street/city/postal_code/country)"));
      return;
    }
    int postal_code = Integer.parseInt(postal_codeStr);
    Address created = repo.create(street, city, postal_code, country);
    HttpUtils.sendJson(ex, 201, Json.toJson(toMap(created)));
  }

  private void update(HttpExchange ex, int id) throws Exception {
    if (id <= 0) {
      HttpUtils.sendJson(ex, 400, Json.error("Invalid ID"));
      return;
    }

    Map<String, String> body = Json.parse(HttpUtils.readBody(ex));
    String street = body.get("street");
    String city = body.get("city");
    String postal_codeStr = body.get("postal_code");
    String country = body.get("country");

    if (street == null || city == null || postal_codeStr == null || country == null) {
      HttpUtils.sendJson(ex, 400, "All fields required(street/city/postal_code/country)");
      return;
    }

    int postal_code = Integer.parseInt(postal_codeStr);
    Optional<Address> updated = repo.update(id, street, city, postal_code, country);
    if (updated.isEmpty()) {
      HttpUtils.sendJson(ex, 404, Json.error("User Unknown"));
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
      HttpUtils.sendJson(ex, 404, Json.error("Address not found"));
      return;
    }
    HttpUtils.sendNoContent(ex);
  }

  private Map<String, Object> toMap(Address a) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", a.id);
    m.put("street", a.street);
    m.put("city", a.city);
    m.put("postal_code", a.postal_code);
    m.put("country", a.country);
    return m;
  }
}
