
package controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

import db.TransactionRepository;
import model.Transaction;
import model.enums.TransactionStatus;
import util.HttpUtils;
import util.Json;

public class TransactionController {

  private final TransactionRepository repo = new TransactionRepository();

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
    List<Transaction> transactions = repo.findAll();
    List<Map<String, Object>> list = transactions.stream().map(this::toMap).toList();
    HttpUtils.sendJson(ex, 200, Json.toJson(list));
  }

  private void getOne(HttpExchange ex, int id) throws Exception {
    Optional<Transaction> transaction = repo.findbyId(id);
    if (transaction.isEmpty()) {
      HttpUtils.sendJson(ex, 404, "Unknown Transaction");
      return;
    }
    HttpUtils.sendJson(ex, 200, Json.toJson(toMap(transaction.get())));
  }

  private void create(HttpExchange ex) throws Exception {
    Map<String, String> body = Json.parse(HttpUtils.readBody(ex));
    String contract_idStr = body.get("contract_id");
    String amounntStr = body.get("amount");
    String payment_date = body.get("payment_date");
    String payment_method = body.get("payment_method");
    String statusStr = body.get("status");
    if (contract_idStr == null || amounntStr == null || payment_date == null || payment_method == null
        || statusStr == null) {
      HttpUtils.sendJson(ex, 400, "All fields required (contract_id/amount/payment_date/payment_method/status)");
      return;
    }
    int contract_id = Integer.parseInt(contract_idStr);
    int amount = Integer.parseInt(amounntStr);
    TransactionStatus status = TransactionStatus.valueOf(statusStr);
    Transaction created = repo.create(contract_id, amount, payment_date, payment_method, status);
    HttpUtils.sendJson(ex, 201, Json.toJson(toMap(created)));
  }

  private void update(HttpExchange ex, int id) throws Exception {
    if (id <= 0) {
      HttpUtils.sendJson(ex, 400, Json.error("Invalid ID"));
      return;
    }
    Map<String, String> body = Json.parse(HttpUtils.readBody(ex));
    String contract_idStr = body.get("contract_id");
    String amounntStr = body.get("amount");
    String payment_date = body.get("payment_date");
    String payment_method = body.get("payment_method");
    String statusStr = body.get("status");
    if (contract_idStr == null || amounntStr == null || payment_date == null || payment_method == null
        || statusStr == null) {
      HttpUtils.sendJson(ex, 400, "All fields required (contract_id/amount/payment_date/payment_method/status)");
      return;
    }
    int contract_id = Integer.parseInt(contract_idStr);
    int amount = Integer.parseInt(amounntStr);
    TransactionStatus status = TransactionStatus.valueOf(statusStr);
    Optional<Transaction> updated = repo.update(id, contract_id, amount, payment_date, payment_method, status);
    if (updated.isEmpty()) {
      HttpUtils.sendJson(ex, 404, "Transaction Unknown");
      return;
    }
    HttpUtils.sendJson(ex, 200, Json.toJson(toMap(updated.get())));
  }

  private void delete(HttpExchange ex, int id) throws Exception {
    if (id <= 0) {
      HttpUtils.sendJson(ex, 200, Json.error("Invalid ID"));
      return;
    }
    if (!repo.delete(id)) {
      HttpUtils.sendJson(ex, 404, "Transaction not found");
      return;
    }
    HttpUtils.sendNoContent(ex);
  }

  private Map<String, Object> toMap(Transaction t) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", t.id);
    m.put("contract_id", t.contract_id);
    m.put("amount", t.amount);
    m.put("payment_date", t.payment_date);
    m.put("payment_method", t.payment_method);
    m.put("status", t.status);
    return m;
  }
}
