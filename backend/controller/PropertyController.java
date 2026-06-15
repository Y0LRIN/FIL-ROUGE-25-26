package controller;

import db.PropertyRepository;
import db.AgentRepository;
import model.Agent;
import model.enums.PropertyType;
import model.enums.PropertyStatus;
import model.Property;
import util.AuthService;
import util.HttpUtils;
import util.Json;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PropertyController {

  private final PropertyRepository repo = new PropertyRepository();

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
    List<Property> properties = repo.findAll();
    List<Map<String, Object>> list = properties.stream().map(this::toMap).toList();
    HttpUtils.sendJson(ex, 200, Json.toJson(list));
  }

  private void getOne(HttpExchange ex, int id) throws Exception {
    Optional<Property> property = repo.findbyId(id);
    if (property.isEmpty()) {
      HttpUtils.sendJson(ex, 404, Json.error("Unknown Property"));
      return;
    }
    HttpUtils.sendJson(ex, 200, Json.toJson(toMap(property.get())));
  }

  private void create(HttpExchange ex) throws Exception {
    Agent current = authenticateAgent(ex);
    if (current == null) {
      return;
    }

    Map<String, String> body = Json.parse(HttpUtils.readBody(ex));
    String title = body.get("title");
    String description = body.get("description");
    String priceStr = body.get("price");
    String surfaceStr = body.get("surface");
    String roomsStr = body.get("rooms");
    String typeStr = body.get("type");
    String statusStr = body.get("status");
    String agent_idStr = body.get("agent_id");
    String address_idStr = body.get("address_id");
    String created_at = body.get("created_at");
    if (title == null || description == null || priceStr == null || surfaceStr == null || roomsStr == null
        || typeStr == null || statusStr == null || address_idStr == null || created_at == null) {
      HttpUtils.sendJson(ex, 400, Json.error(
          "All fields required (title/description/price/surface/rooms/type/status/address_id/created_at)"));
      return;
    }
    int price = Integer.parseInt(priceStr);
    float surface = Float.parseFloat(surfaceStr);
    int rooms = Integer.parseInt(roomsStr);
    PropertyType type = PropertyType.valueOf(typeStr);
    PropertyStatus status = PropertyStatus.valueOf(statusStr);
    int agent_id = current.is_admin ? Integer.parseInt(agent_idStr != null ? agent_idStr : String.valueOf(current.id)) : current.id;
    int address_id = Integer.parseInt(address_idStr);
    Property created = repo.create(title, description, price, surface, rooms, type, status, agent_id, address_id,
        created_at);
    HttpUtils.sendJson(ex, 201, Json.toJson(toMap(created)));
  }

  private void update(HttpExchange ex, int id) throws Exception {
    if (id <= 0) {
      HttpUtils.sendJson(ex, 400, Json.error("Invalid ID"));
      return;
    }

    Agent current = authenticateAgent(ex);
    if (current == null) {
      return;
    }

    Optional<Property> existing = repo.findbyId(id);
    if (existing.isEmpty()) {
      HttpUtils.sendJson(ex, 404, Json.error("Property Unknown"));
      return;
    }
    Property existingProperty = existing.get();
    if (!current.is_admin && existingProperty.agent_id != current.id) {
      HttpUtils.sendJson(ex, 403, Json.error("Forbidden"));
      return;
    }

    Map<String, String> body = Json.parse(HttpUtils.readBody(ex));
    String title = body.get("title");
    String description = body.get("description");
    String priceStr = body.get("price");
    String surfaceStr = body.get("surface");
    String roomsStr = body.get("rooms");
    String typeStr = body.get("type");
    String statusStr = body.get("status");
    String agent_idStr = body.get("agent_id");
    String address_idStr = body.get("address_id");
    String created_at = body.get("created_at");
    if (title == null || description == null || priceStr == null || surfaceStr == null || roomsStr == null
        || typeStr == null || statusStr == null || address_idStr == null || created_at == null) {
      HttpUtils.sendJson(ex, 400, Json.error(
          "All fields required (title/description/price/surface/rooms/type/status/address_id/created_at)"));
      return;
    }
    int price = Integer.parseInt(priceStr);
    float surface = Float.parseFloat(surfaceStr);
    int rooms = Integer.parseInt(roomsStr);
    PropertyType type = PropertyType.valueOf(typeStr);
    PropertyStatus status = PropertyStatus.valueOf(statusStr);
    int agent_id = current.is_admin ? Integer.parseInt(agent_idStr != null ? agent_idStr : String.valueOf(existingProperty.agent_id)) : existingProperty.agent_id;
    int address_id = Integer.parseInt(address_idStr);
    Optional<Property> updated = repo.update(id, title, description, price, surface, rooms, type, status, agent_id,
        address_id, created_at);
    if (updated.isEmpty()) {
      HttpUtils.sendJson(ex, 404, Json.error("Property Unknown"));
      return;
    }
    HttpUtils.sendJson(ex, 200, Json.toJson(toMap(updated.get())));
  }

  private void delete(HttpExchange ex, int id) throws Exception {
    if (id <= 0) {
      HttpUtils.sendJson(ex, 400, Json.error("Invalid ID"));
      return;
    }
    Agent current = authenticateAgent(ex);
    if (current == null) {
      return;
    }

    Optional<Property> existing = repo.findbyId(id);
    if (existing.isEmpty()) {
      HttpUtils.sendJson(ex, 404, Json.error("Property not found"));
      return;
    }
    if (!current.is_admin && existing.get().agent_id != current.id) {
      HttpUtils.sendJson(ex, 403, Json.error("Forbidden"));
      return;
    }

    if (!repo.delete(id)) {
      HttpUtils.sendJson(ex, 404, Json.error("Property not found"));
      return;
    }
    HttpUtils.sendNoContent(ex);
  }

  private Map<String, Object> toMap(Property p) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", p.id);
    m.put("title", p.title);
    m.put("description", p.description);
    m.put("price", p.price);
    m.put("surface", p.surface);
    m.put("rooms", p.rooms);
    m.put("type", p.type);
    m.put("status", p.status);
    m.put("agent_id", p.agent_id);
    m.put("address_id", p.address_id);
    m.put("created_at", p.created_at);
    return m;
  }

  private Agent authenticateAgent(HttpExchange ex) throws Exception {
    String token = HttpUtils.getBearerToken(ex);
    if (token == null || !AuthService.isTokenValid(token)) {
      HttpUtils.sendJson(ex, 401, Json.error("Unauthorized"));
      return null;
    }
    int agentId = AuthService.getAgentId(token);
    Optional<Agent> agent = new AgentRepository().findbyId(agentId);
    if (agent.isEmpty()) {
      HttpUtils.sendJson(ex, 401, Json.error("Unauthorized"));
      return null;
    }
    return agent.get();
  }
}
