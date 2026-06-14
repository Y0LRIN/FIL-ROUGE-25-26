package controller;

import db.VisitRepository;
import model.Visit;
import util.HttpUtils;
import util.Json;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class VisitController {

  private final VisitRepository repo = new VisitRepository();

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
    List<Visit> visits = repo.findAll();
    List<Map<String, Object>> list = visits.stream().map(this::toMap).toList();
    HttpUtils.sendJson(ex, 200, Json.toJson(list));
  }

  private void getOne(HttpExchange ex, int id) throws Exception {
    Optional<Visit> visit = repo.findbyId(id);
    if (visit.isEmpty()) {
      HttpUtils.sendJson(ex, 404, Json.error("Unknown Visit"));
      return;
    }
    HttpUtils.sendJson(ex, 200, Json.toJson(toMap(visit.get())));
  }

  private void create(HttpExchange ex) throws Exception {
    Map<String, String> body = Json.parse(HttpUtils.readBody(ex));
    String property_idStr = body.get("property_id");
    String client_idStr = body.get("client_id");
    String agent_idStr = body.get("agent_id");
    String visit_date = body.get("visit_date");
    String feedback = body.get("feedback");
    if (property_idStr == null || client_idStr == null || agent_idStr == null || visit_date == null
        || feedback == null) {
      HttpUtils.sendJson(ex, 400,
          Json.error("All fields required (property_id/client_id/agent_id/visit_date/feedback"));
      return;
    }
    int property_id = Integer.parseInt(property_idStr);
    int client_id = Integer.parseInt(client_idStr);
    int agent_id = Integer.parseInt(agent_idStr);
    Visit created = repo.create(property_id, client_id, agent_id, visit_date, feedback);
    HttpUtils.sendJson(ex, 201, Json.toJson(toMap(created)));
  }

  private void update(HttpExchange ex, int id) throws Exception {
    if (id <= 0) {
      HttpUtils.sendJson(ex, 400, Json.error("Invalid ID"));
      return;
    }
    Map<String, String> body = Json.parse(HttpUtils.readBody(ex));
    String property_idStr = body.get("property_id");
    String client_idStr = body.get("client_id");
    String agent_idStr = body.get("agent_id");
    String visit_date = body.get("visit_date");
    String feedback = body.get("feedback");
    if (property_idStr == null || client_idStr == null || agent_idStr == null || visit_date == null
        || feedback == null) {
      HttpUtils.sendJson(ex, 400,
          Json.error("All fields required (property_id/client_id/agent_id/visit_date/feedback"));
      return;
    }
    int property_id = Integer.parseInt(property_idStr);
    int client_id = Integer.parseInt(client_idStr);
    int agent_id = Integer.parseInt(agent_idStr);
    Optional<Visit> updated = repo.update(id, property_id, client_id, agent_id, visit_date, feedback);
    if (updated.isEmpty()) {
      HttpUtils.sendJson(ex, 404, Json.error("Visit Unknown"));
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
      HttpUtils.sendJson(ex, 404, Json.error("Visit not found"));
      return;
    }
    HttpUtils.sendNoContent(ex);
  }

  private Map<String, Object> toMap(Visit v) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", v.id);
    m.put("property_id", v.property_id);
    m.put("client_id", v.client_id);
    m.put("agent_id", v.agent_id);
    m.put("visit_date", v.visit_date);
    m.put("feedback", v.feedback);
    return m;
  }
}
