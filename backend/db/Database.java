package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// Class to connect and initialise the database
public class Database {

  // URL for the database
  private static final String URL = "jdbc:sqlite:filrouge.db";

  private static Connection connection;

  // Connection
  public static void init() throws SQLException {
    init(URL);
  }

  public static void init(String url) throws SQLException {
    connection = DriverManager.getConnection(url);
    try (Statement pragma = connection.createStatement()) {
      pragma.execute("PRAGMA foreign_keys = ON");
    }
    System.out.println("Database connexion established : " + url);
    createSchema();
  }

  private static void ensureColumnExists(String table, String column, String definition) throws SQLException {
    try (Statement st = connection.createStatement();
        java.sql.ResultSet rs = st.executeQuery("PRAGMA table_info(" + table + ")")) {
      boolean found = false;
      while (rs.next()) {
        if (column.equals(rs.getString("name"))) {
          found = true;
          break;
        }
      }
      if (!found) {
        st.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
      }
    }
  }

  public static Connection get() {
    return connection;
  }

  public static void close() throws SQLException {
    if (connection != null && !connection.isClosed()) {
      connection.close();
    }
    connection = null;
  }

  // Create tables if they don't exist
  private static void createSchema() throws SQLException {
    try (Statement st = connection.createStatement()) {
      st.executeUpdate("""
          CREATE TABLE IF NOT EXISTS clients (
            id          INTEGER  NOT NULL  PRIMARY KEY  AUTOINCREMENT,
            first_name  TEXT     NOT NULL,
            last_name   TEXT     NOT NULL,
            email       TEXT     NOT NULL,
            phone       TEXT     NOT NULL,
            type        TEXT     NOT NULL  CHECK(type IN ('BUYER', 'SELLER', 'TENANT', 'LANDLORD'))
          )
          """);

      st.executeUpdate("""
          CREATE TABLE IF NOT EXISTS agents (
            id           INTEGER  NOT NULL  PRIMARY KEY  AUTOINCREMENT,
            name         TEXT     NOT NULL,
            email        TEXT     NOT NULL,
            phone        TEXT     NOT NULL,
            is_admin     INTEGER  NOT NULL  CHECK (is_admin IN (0,1)),
            password_hash TEXT    NOT NULL DEFAULT '',
            created_at   TEXT     NOT NULL  DEFAULT (date('now'))
          )
          """);

      st.executeUpdate("""
              CREATE TABLE IF NOT EXISTS addresses (
                id           INTEGER  NOT NULL  PRIMARY KEY  AUTOINCREMENT,
                street       TEXT     NOT NULL,
                city         TEXT     NOT NULL,
                postal_code  TEXT     NOT NULL,
                country      TEXT     NOT NULL
            )
          """);

      st.executeUpdate("""
              CREATE TABLE IF NOT EXISTS properties (
                id           INTEGER  NOT NULL  PRIMARY KEY  AUTOINCREMENT,
                title        TEXT     NOT NULL,
                description  TEXT     NOT NULL,
                price        INTEGER  NOT NULL,
                surface      FLOAT    NOT NULL,
                rooms        INTEGER  NOT NULL,
                type         TEXT     NOT NULL  CHECK (type IN ('HOUSE', 'APARTMENT', 'LAND', 'COMMERCIAL')),
                status       TEXT     NOT NULL  CHECK (status IN ('AVAILABLE', 'SOLD', 'RENTED', 'PENDING')),
                agent_id     INTEGER  NOT NULL  REFERENCES agents(id)     ON DELETE CASCADE,
                address_id   INTEGER  NOT NULL  REFERENCES addresses(id)  ON DELETE CASCADE,
                created_at   TEXT     NOT NULL  DEFAULT (date('now'))
            )
          """);

      st.executeUpdate("""
            CREATE TABLE IF NOT EXISTS property_images (
              id           INTEGER  NOT NULL  PRIMARY KEY  AUTOINCREMENT,
              property_id  INTEGER  NOT NULL  REFERENCES properties(id)  ON DELETE CASCADE,
              image_url    TEXT     NOT NULL,
              is_main      INTEGER  NOT NULL  CHECK (is_main IN (0,1)),
              created_at   TEXT     NOT NULL  DEFAULT (date('now'))
          )
          """);

      ensureColumnExists("agents", "password_hash", "TEXT NOT NULL DEFAULT ''");

      st.executeUpdate("""
            CREATE TABLE IF NOT EXISTS favorites (
              id           INTEGER  NOT NULL  PRIMARY KEY  AUTOINCREMENT,
              client_id    INTEGER  NOT NULL  REFERENCES clients(id)     ON DELETE CASCADE,
              property_id  INTEGER  NOT NULL  REFERENCES properties(id)  ON DELETE CASCADE,
              created_at   TEXT     NOT NULL  DEFAULT (date('now'))
          )
          """);

      st.executeUpdate("""
            CREATE TABLE IF NOT EXISTS visits (
              id           INTEGER  NOT NULL  PRIMARY KEY  AUTOINCREMENT,
              property_id  INTEGER  NOT NULL  REFERENCES properties(id)  ON DELETE CASCADE,
              client_id    INTEGER  NOT NULL  REFERENCES clients(id)     ON DELETE CASCADE,
              agent_id     INTEGER  NOT NULL  REFERENCES agents(id)      ON DELETE CASCADE,
              visit_date   TEXT     NOT NULL  CHECK (visit_date LIKE '____-__-__'),
              feedback     TEXT     NOT NULL
          )
          """);

      st.executeUpdate("""
            CREATE TABLE IF NOT EXISTS contracts (
              id           INTEGER  NOT NULL  PRIMARY KEY  AUTOINCREMENT,
              property_id  INTEGER  NOT NULL  REFERENCES properties(id)  ON DELETE CASCADE,
              client_id    INTEGER  NOT NULL  REFERENCES clients(id)     ON DELETE CASCADE,
              agent_id     INTEGER  NOT NULL  REFERENCES agents(id)      ON DELETE CASCADE,
              type         TEXT     NOT NULL  CHECK (type IN ('SALE', 'RENTAL')),
              signed_at    TEXT     NOT NULL  CHECK (signed_at LIKE '____-__-__')
          )
          """);

      st.executeUpdate("""
            CREATE TABLE IF NOT EXISTS transactions (
              id              INTEGER  NOT NULL  PRIMARY KEY  AUTOINCREMENT,
              contract_id     INTEGER  NOT NULL  REFERENCES contracts(id)  ON DELETE CASCADE,
              amount          INTEGER  NOT NULL,
              payment_date    TEXT     NOT NULL  CHECK (payment_date LIKE '____-__-__'),
              payment_method  TEXT     NOT NULL,
              status          TEXT     NOT NULL  CHECK (status IN ('PAID', 'PENDING', 'CANCELLED'))
          )
          """);
    }
    System.out.println("Database shema OK");
  }
}
