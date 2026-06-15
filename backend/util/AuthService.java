package util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public class AuthService {

  private static final Map<String, Integer> sessions = new ConcurrentHashMap<>();

  public static String createToken(int agentId) {
    String token = UUID.randomUUID().toString();
    sessions.put(token, agentId);
    return token;
  }

  public static Integer getAgentId(String token) {
    return sessions.get(token);
  }

  public static boolean isTokenValid(String token) {
    return token != null && sessions.containsKey(token);
  }

  public static void revokeToken(String token) {
    if (token != null) {
      sessions.remove(token);
    }
  }
}
