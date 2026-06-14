package controller;

import db.ContractRepository;
import model.Contract;
import model.enums.ContractType;
import util.HttpUtils;
import util.Json;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ContractController {

  private final ContractRepository repo = new ContractRepository();

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
    List<Contract> contracts = repo.findAll();
    List<Map<String, Object>> list = contracts.stream().map(this::toMap).toList();
    HttpUtils.sendJson(ex, 200, Json.toJson(list));
  }

  private void getOne(HttpExchange ex, int id) throws Exception {
    Optional<Contract> contract = repo.findbyId(id);
    if (contract.isEmpty()) {
      HttpUtils.sendJson(ex, 404, Json.error("Unknown Contract"));
      return;
    }
    HttpUtils.sendJson(ex, 200, Json.toJson(toMap(contract.get())));
  }

  private void create(HttpExchange ex) throws Exception {
    Map<String, String> body = Json.parse(HttpUtils.readBody(ex));
    String property_idStr = body.get("property_id");
    String client_idStr = body.get("client_id");
    String agent_idStr = body.get("agent_id");
    String typeStr = body.get("type");
    String signed_at = body.get("signed_at");
    if (property_idStr == null || client_idStr == null || agent_idStr == null || typeStr == null || signed_at == null) {
      HttpUtils.sendJson(ex, 400, Json.error("All fields required (property_id/client_id/agent_id/type/signed_at)"));
      return;
    }
    int property_id = Integer.parseInt(property_idStr);
    int client_id = Integer.parseInt(client_idStr);
    int agent_id = Integer.parseInt(agent_idStr);
    ContractType type = ContractType.valueOf(typeStr);
    Contract created = repo.create(property_id, client_id, agent_id, type, signed_at);
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
    String typeStr = body.get("type");
    String signed_at = body.get("signed_at");
    if (property_idStr == null || client_idStr == null || agent_idStr == null || typeStr == null || signed_at == null) {
      HttpUtils.sendJson(ex, 400, Json.error("All fields required (property_id/client_id/agent_id/type/signed_at)"));
      return;
    }
    int property_id = Integer.parseInt(property_idStr);
    int client_id = Integer.parseInt(client_idStr);
    int agent_id = Integer.parseInt(agent_idStr);
    ContractType type = ContractType.valueOf(typeStr);
    Optional<Contract> updated = repo.update(id, property_id, client_id, agent_id, type, signed_at);
    if (updated.isEmpty()) {
      HttpUtils.sendJson(ex, 404, Json.error("Contract Unknown"));
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
      HttpUtils.sendJson(ex, 404, Json.error("Contract not found"));
      return;
    }
    HttpUtils.sendNoContent(ex);
  }

  private Map<String, Object> toMap(Contract c) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", c.id);
    m.put("property_id", c.property_id);
    m.put("client_id", c.client_id);
    m.put("agent_id", c.agent_id);
    m.put("type", c.type);
    m.put("signed_at", c.signed_at);
    return m;
  }
}
