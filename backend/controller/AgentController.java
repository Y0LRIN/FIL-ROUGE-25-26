package controller;

import db.AgentRepository;
import model.Agent;
import util.HttpUtils;
import util.Json;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import util.AuthService;
import util.PasswordUtils;

public class AgentController {

  private final AgentRepository repo = new AgentRepository();

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
        default -> HttpUtils.sendJson(ex, 405, Json.error("Unknown method"));
      }
    } catch (Exception e) {
      HttpUtils.sendJson(ex, 500, Json.error(e.getMessage()));
    }
  }

  // HANDLERS

  private void getAll(HttpExchange ex) throws Exception {
    Agent current = authenticateAgent(ex);
    if (current == null) {
      return;
    }
    if (!current.is_admin) {
      HttpUtils.sendJson(ex, 403, Json.error("Forbidden"));
      return;
    }
    List<Agent> agents = repo.findAll();
    List<Map<String, Object>> list = agents.stream().map(this::toMap).toList();
    HttpUtils.sendJson(ex, 200, Json.toJson(list));
  }

  private void getOne(HttpExchange ex, int id) throws Exception {
    Agent current = authenticateAgent(ex);
    if (current == null) {
      return;
    }
    Optional<Agent> agent = repo.findbyId(id);
    if (agent.isEmpty()) {
      HttpUtils.sendJson(ex, 404, Json.error("Unknown agent"));
      return;
    }
    if (!current.is_admin && current.id != id) {
      HttpUtils.sendJson(ex, 403, Json.error("Forbidden"));
      return;
    }
    HttpUtils.sendJson(ex, 200, Json.toJson(toMap(agent.get())));
  }

  private void create(HttpExchange ex) throws Exception {
    boolean hasAgents = !repo.findAll().isEmpty();
    Agent current = null;
    if (hasAgents) {
      current = authenticateAgent(ex);
      if (current == null) {
        return;
      }
      if (!current.is_admin) {
        HttpUtils.sendJson(ex, 403, Json.error("Forbidden"));
        return;
      }
    }

    Map<String, String> body = Json.parse(HttpUtils.readBody(ex));
    String name = body.get("name");
    String email = body.get("email");
    String phone = body.get("phone");
    String is_adminStr = body.get("is_admin");
    String passwordHash = body.get("password_hash");
    if (passwordHash == null) {
      passwordHash = body.get("password");
    }
    String created_at = body.get("created_at");
    if (name == null || email == null || phone == null || is_adminStr == null || created_at == null) {
      HttpUtils.sendJson(ex, 400, Json.error("All fields required (name/email/phone/is_admin/created_at)"));
      return;
    }
    boolean is_admin = Boolean.parseBoolean(is_adminStr);
    String storedPassword = passwordHash == null || passwordHash.isEmpty() ? "" : PasswordUtils.createStoredPassword(passwordHash);
    Agent created = repo.create(name, email, phone, is_admin, storedPassword, created_at);
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
    if (!current.is_admin && current.id != id) {
      HttpUtils.sendJson(ex, 403, Json.error("Forbidden"));
      return;
    }

    Map<String, String> body = Json.parse(HttpUtils.readBody(ex));
    String name = body.get("name");
    String email = body.get("email");
    String phone = body.get("phone");
    String is_adminStr = body.get("is_admin");
    String passwordHash = body.get("password_hash");
    if (passwordHash == null) {
      passwordHash = body.get("password");
    }
    String created_at = body.get("created_at");

    if (name == null || email == null || phone == null || is_adminStr == null || created_at == null) {
      HttpUtils.sendJson(ex, 400, Json.error("All fields required(name/email/phone/is_admin/created_at)"));
      return;
    }

    String storedPassword;
    if (passwordHash == null || passwordHash.isEmpty()) {
      Optional<Agent> existing = repo.findbyId(id);
      if (existing.isEmpty()) {
        HttpUtils.sendJson(ex, 404, Json.error("Agent Unknown"));
        return;
      }
      storedPassword = existing.get().password_hash;
    } else {
      storedPassword = PasswordUtils.createStoredPassword(passwordHash);
    }

    boolean is_admin = Boolean.parseBoolean(is_adminStr);
    Optional<Agent> updated = repo.update(id, name, email, phone, is_admin, storedPassword, created_at);
    if (updated.isEmpty()) {
      HttpUtils.sendJson(ex, 404, Json.error("Agent Unknown"));
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
    if (!current.is_admin) {
      HttpUtils.sendJson(ex, 403, Json.error("Forbidden"));
      return;
    }

    if (!repo.delete(id)) {
      HttpUtils.sendJson(ex, 404, Json.error("Agent not found"));
      return;
    }
    HttpUtils.sendNoContent(ex);
  }

  private Agent authenticateAgent(HttpExchange ex) throws Exception {
    String token = HttpUtils.getBearerToken(ex);
    if (token == null || !AuthService.isTokenValid(token)) {
      HttpUtils.sendJson(ex, 401, Json.error("Unauthorized"));
      return null;
    }
    int agentId = AuthService.getAgentId(token);
    Optional<Agent> agent = repo.findbyId(agentId);
    if (agent.isEmpty()) {
      HttpUtils.sendJson(ex, 401, Json.error("Unauthorized"));
      return null;
    }
    return agent.get();
  }

  private Map<String, Object> toMap(Agent a) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", a.id);
    m.put("name", a.name);
    m.put("email", a.email);
    m.put("phone", a.phone);
    m.put("is_admin", a.is_admin);
    m.put("created_at", a.created_at);
    return m;
  }
}
