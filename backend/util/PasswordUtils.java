package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordUtils {

  public static String hashSha256(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashed = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      return toHex(hashed);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  public static String createSalt() {
    byte[] salt = new byte[16];
    try {
      SecureRandom.getInstanceStrong().nextBytes(salt);
    } catch (Exception e) {
      new SecureRandom().nextBytes(salt);
    }
    return toHex(salt);
  }

  public static String hashWithSalt(String clientHash, String salt) {
    return hashSha256(salt + clientHash);
  }

  public static String createStoredPassword(String clientHash) {
    String salt = createSalt();
    return salt + "$" + hashWithSalt(clientHash, salt);
  }

  public static boolean verify(String clientHash, String storedPassword) {
    if (storedPassword == null || clientHash == null || !storedPassword.contains("$")) {
      return false;
    }
    String[] parts = storedPassword.split("\\$", 2);
    if (parts.length != 2) {
      return false;
    }
    String salt = parts[0];
    String expectedHash = parts[1];
    return expectedHash.equals(hashWithSalt(clientHash, salt));
  }

  private static String toHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }
}
