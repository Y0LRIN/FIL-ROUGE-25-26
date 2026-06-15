package util;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class HttpUtils {

  public static String readBody(HttpExchange ex) throws IOException {
    try (InputStream is = ex.getRequestBody()) {
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  public static void setCorsHeaders(HttpExchange ex) {
    ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
    ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Accept, Authorization");
  }

  public static String getBearerToken(HttpExchange ex) {
    String authHeader = ex.getRequestHeaders().getFirst("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return null;
    }
    return authHeader.substring(7);
  }

  public static void sendJson(HttpExchange ex, int status, String json) throws IOException {
    byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
    ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
    setCorsHeaders(ex);
    ex.sendResponseHeaders(status, bytes.length);
    try (OutputStream os = ex.getResponseBody()) {
      os.write(bytes);
    }
  }

  public static void sendNoContent(HttpExchange ex) throws IOException {
    setCorsHeaders(ex);
    ex.sendResponseHeaders(204, -1);
    ex.getResponseBody().close();
  }

  public static void sendOptions(HttpExchange ex) throws IOException {
    setCorsHeaders(ex);
    ex.sendResponseHeaders(204, -1);
    ex.getResponseBody().close();
  }

  public static String segment(HttpExchange ex, int index) {
    String[] parts = ex.getRequestURI().getPath().split("/");
    return (index < parts.length) ? parts[index] : null;
  }

  public static int segmentInt(HttpExchange ex, int index) {
    try {
      return Integer.parseInt(segment(ex, index));
    } catch (NumberFormatException | NullPointerException e) {
      return -1;
    }
  }
}
