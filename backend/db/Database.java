package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

  private static final String URL = "";

  private static Connection connection;

  public static void init() throws SQLException {
    connection = DriverManager.getConnection(URL);
    System.out.println("Database connexion established : " + URL);
    createSchema();
  }

  public static Connection get() {
    return connection;
  }

  private static void createSchema() throws SQLException {
    try (Statement st = connection.createStatement()) {
      st.executeUpdate("""
          CREATE TABLE IF NOT EXISTS users (
            id          INTEGER  PRIMARY KEY  AUTOINCREMENT,
            name        TEXT     NOT NULL,
            email       TEXT     NOT NULL  UNIQUE,
            created_at  TEXT     DEFAULT (datetime('now'))
          )
          """);

      st.executeUpdate("""
          CREATE TABLE IF NOT EXISTS tasks (
            id       INTEGER  PRIMARY KEY  AUTOINCREMENT,
            user_id  INTEGER  NOT NULL,
            title    TEXT     NOT NULL,
            done     INTEGER  NOT NULL DEFAULT 0,
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
          )
          """);
    }
    System.out.println("Database shema OK");
  }
}
