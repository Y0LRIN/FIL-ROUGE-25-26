package controller;

import db.FavoriteRepository;
import model.Favorite;
import util.HttpUtils;
import util.Json;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FavoriteController {

  private final FavoriteRepository repo = new FavoriteRepository();

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
    List<Favorite> favorites = repo.findAll();
    List<Map<String, Object>> list = favorites.stream().map(this::toMap).toList();
    HttpUtils.sendJson(ex, 200, Json.toJson(list));
  }

  private void getOne(HttpExchange ex, int id) throws Exception {
    Optional<Favorite> favorite = repo.findbyId(id);
    if (favorite.isEmpty()) {
      HttpUtils.sendJson(ex, 404, "Unknown Favorite");
      return;
    }
    HttpUtils.sendJson(ex, 200, Json.toJson(toMap(favorite.get())));
  }

  private void create(HttpExchange ex) throws Exception {
    Map<String, String> body = Json.parse(HttpUtils.readBody(ex));
    String client_idStr = body.get("client_id");
    String property_idStr = body.get("property_id");
    String created_at = body.get("created_at");
    if (client_idStr == null || property_idStr == null || created_at == null) {
      HttpUtils.sendJson(ex, 400, Json.error("All fields required(client_id/property_id/created_at)"));
      return;
    }
    int client_id = Integer.parseInt(client_idStr);
    int property_id = Integer.parseInt(property_idStr);
    Favorite created = repo.create(client_id, property_id, created_at);
    HttpUtils.sendJson(ex, 201, Json.toJson(toMap(created)));
  }

  private void update(HttpExchange ex, int id) throws Exception {
    if (id <= 0) {
      HttpUtils.sendJson(ex, 400, Json.error("Invalid ID"));
      return;
    }

    Map<String, String> body = Json.parse(HttpUtils.readBody(ex));
    String client_idStr = body.get("client_id");
    String property_idStr = body.get("property_id");
    String created_at = body.get("created_at");
    if (client_idStr == null || property_idStr == null || created_at == null) {
      HttpUtils.sendJson(ex, 400, "All fields required (client_id/property_id/created_at)");
      return;
    }
    int client_id = Integer.parseInt(client_idStr);
    int property_id = Integer.parseInt(property_idStr);
    Optional<Favorite> updated = repo.update(id, client_id, property_id, created_at);
    if (updated.isEmpty()) {
      HttpUtils.sendJson(ex, 404, Json.error("Favorite Unknown"));
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
      HttpUtils.sendJson(ex, 404, "Favorite not found");
      return;
    }
    HttpUtils.sendNoContent(ex);
  }

  private Map<String, Object> toMap(Favorite f) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", f.id);
    m.put("client_id", f.client_id);
    m.put("property_id", f.property_id);
    m.put("created_at", f.created_at);
    return m;
  }
}
