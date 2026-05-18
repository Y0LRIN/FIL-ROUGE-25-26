package util;

import java.util.*;
import java.util.regex.*;

public class Json {

  public static String toJson(Map<String, Object> map) {
    StringBuilder sb = new StringBuilder("{");
    Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, Object> e = it.next();
      sb.append('"').append(escape(e.getKey())).append('"').append(':');
      sb.append(value(e.getValue()));
      if (it.hasNext()) {
        sb.append(',');
      }
    }
    return sb.append('}').toString();
  }

  public static String toJson(List<Map<String, Object>> list) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < list.size(); i++) {
      sb.append(toJson(list.get(i)));
      if (i < list.size() - 1) {
        sb.append(',');
      }
    }
    return sb.append(']').toString();
  }

  private static String value(Object v) {
    if (v == null)
      return "null";
    if (v instanceof Number)
      return v.toString();
    if (v instanceof Boolean)
      return v.toString();
    return '"' + escape(v.toString()) + '"';
  }

  private static String escape(String s) {
    return s.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
  }

  public static Map<String, String> parse(String json) {
    Map<String, String> map = new LinkedHashMap<>();
    if (json == null || json.isBlank()) {
      return map;
    }
    Pattern p = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(\"([^\"]*)\"|(-?\\d+\\.?\\d*)|true|false|null)");
    Matcher m = p.matcher(json);
    while (m.find()) {
      String key = m.group(1);
      String raw = m.group(2);
      String strVal = m.group(3);
      map.put(key, strVal != null ? strVal : raw);
    }
    return map;
  }

  public static String error(String message) {
    return "{\"error\":\"" + escape(message) + "\"}";
  }
}
