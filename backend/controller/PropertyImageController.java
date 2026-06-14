package controller;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;

import db.PropertyImageRepository;
import model.PropertyImage;
import util.HttpUtils;
import util.Json;

public class PropertyImageController {

  private final PropertyImageRepository repo = new PropertyImageRepository();

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
    List<PropertyImage> propertyImages = repo.findAll();
    List<Map<String, Object>> list = propertyImages.stream().map(this::toMap).toList();
    HttpUtils.sendJson(ex, 200, Json.toJson(list));
  }

  private void getOne(HttpExchange ex, int id) throws Exception {
    Optional<PropertyImage> propertyImage = repo.findbyId(id);
    if (propertyImage.isEmpty()) {
      HttpUtils.sendJson(ex, 404, Json.error("Unknown PropertyImage"));
      return;
    }
    HttpUtils.sendJson(ex, 200, Json.toJson(toMap(propertyImage.get())));
  }

  private void create(HttpExchange ex) throws Exception {
    Map<String, String> body = Json.parse(HttpUtils.readBody(ex));
    String property_idStr = body.get("property_id");
    String image_url = body.get("image_url");
    String is_mainStr = body.get("is_main");
    String created_at = body.get("created_at");
    if (property_idStr == null || image_url == null || is_mainStr == null || created_at == null) {
      HttpUtils.sendJson(ex, 400, Json.error("All fields required (property_id/image_url/is_main/created_at)"));
      return;
    }
    int property_id = Integer.parseInt(property_idStr);
    boolean is_main = Boolean.parseBoolean(is_mainStr);
    PropertyImage created = repo.create(property_id, image_url, is_main, created_at);
    HttpUtils.sendJson(ex, 201, Json.toJson(toMap(created)));
  }

  private void update(HttpExchange ex, int id) throws Exception {
    if (id <= 0) {
      HttpUtils.sendJson(ex, 400, Json.error("Invalid ID"));
      return;
    }
    Map<String, String> body = Json.parse(HttpUtils.readBody(ex));
    String property_idStr = body.get("property_id");
    String image_url = body.get("image_url");
    String is_mainStr = body.get("is_main");
    String created_at = body.get("created_at");
    String idStr = body.get("id");
    if (property_idStr == null || image_url == null || is_mainStr == null || created_at == null) {
      HttpUtils.sendJson(ex, 400, Json.error("All fields required (property_id/image_url/is_main/created_at)"));
      return;
    }
    int property_id = Integer.parseInt(property_idStr);
    boolean is_main = Boolean.parseBoolean(is_mainStr);
    Optional<PropertyImage> updated = repo.update(id, property_id, image_url, is_main, created_at);
    if (updated.isEmpty()) {
      HttpUtils.sendJson(ex, 404, Json.error("PropertyImage Unknown"));
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
      HttpUtils.sendJson(ex, 404, Json.error("PropertyImage not found"));
      return;
    }
    HttpUtils.sendNoContent(ex);
  }

  private Map<String, Object> toMap(PropertyImage p) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", p.id);
    m.put("property_id", p.property_id);
    m.put("image_url", p.image_url);
    m.put("is_main", p.is_main);
    m.put("created_at", p.created_at);
    return m;
  }
}
