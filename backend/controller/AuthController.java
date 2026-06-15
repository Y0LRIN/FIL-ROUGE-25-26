package controller;

import db.AgentRepository;
import model.Agent;
import util.AuthService;
import util.HttpUtils;
import util.Json;
import util.PasswordUtils;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class AuthController {

  private final AgentRepository repo = new AgentRepository();

  public void handle(HttpExchange ex) throws IOException {
    try {
      switch (ex.getRequestMethod()) {
        case "POST" -> login(ex);
        default -> HttpUtils.sendJson(ex, 405, Json.error("Method not allowed"));
      }
    } catch (Exception e) {
      HttpUtils.sendJson(ex, 500, Json.error(e.getMessage()));
    }
  }

  private void login(HttpExchange ex) throws Exception {
    Map<String, String> body = Json.parse(HttpUtils.readBody(ex));
    String email = body.get("email");
    String passwordHash = body.get("password_hash");
    if (passwordHash == null) {
      passwordHash = body.get("password");
    }
    if (email == null || passwordHash == null) {
      HttpUtils.sendJson(ex, 400, Json.error("Email and password required"));
      return;
    }

    Optional<Agent> agent = repo.findByEmail(email);
    if (agent.isEmpty() || !PasswordUtils.verify(passwordHash, agent.get().password_hash)) {
      HttpUtils.sendJson(ex, 401, Json.error("Invalid credentials"));
      return;
    }

    String token = AuthService.createToken(agent.get().id);
    HttpUtils.sendJson(ex, 200, Json.toJson(Map.of(
        "token", token,
        "agent", toMap(agent.get())
    )));
  }

  private Map<String, Object> toMap(Agent a) {
    return Map.of(
        "id", a.id,
        "name", a.name,
        "email", a.email,
        "phone", a.phone,
        "is_admin", a.is_admin,
        "created_at", a.created_at
    );
  }
}
