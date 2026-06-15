import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

import controller.AgentController;
import controller.AddressController;
import controller.AuthController;
import controller.ClientController;
import controller.ContractController;
import controller.FavoriteController;
import controller.PropertyController;
import controller.PropertyImageController;
import controller.TransactionController;
import controller.VisitController;
import db.Database;
import util.HttpUtils;
import util.Json;

public class Main {

  public static void main(String[] args) throws Exception {
    Database.init();

    HttpServer server = createServer(8080);
    server.start();
    System.out.println("Server started on http://localhost:8080");
  }

  private static void handleRequest(com.sun.net.httpserver.HttpExchange ex, Object controller) throws Exception {
    if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
      HttpUtils.sendOptions(ex);
      return;
    }

    if (controller instanceof controller.AgentController a) {
      a.handle(ex);
    } else if (controller instanceof controller.AddressController a) {
      a.handle(ex);
    } else if (controller instanceof controller.ClientController c) {
      c.handle(ex);
    } else if (controller instanceof controller.ContractController c) {
      c.handle(ex);
    } else if (controller instanceof controller.FavoriteController f) {
      f.handle(ex);
    } else if (controller instanceof controller.AuthController a) {
      a.handle(ex);
    } else if (controller instanceof controller.PropertyController p) {
      p.handle(ex);
    } else if (controller instanceof controller.PropertyImageController p) {
      p.handle(ex);
    } else if (controller instanceof controller.TransactionController t) {
      t.handle(ex);
    } else if (controller instanceof controller.VisitController v) {
      v.handle(ex);
    } else {
      HttpUtils.sendJson(ex, 404, Json.error("Not Found"));
    }
  }

  public static HttpServer createServer(int port) throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
    server.createContext("/api/agents", ex -> {
      try {
        handleRequest(ex, new AgentController());
      } catch (Exception e) {
        HttpUtils.sendJson(ex, 500, Json.error(e.getMessage()));
      }
    });
    server.createContext("/api/auth", ex -> {
      try {
        handleRequest(ex, new AuthController());
      } catch (Exception e) {
        HttpUtils.sendJson(ex, 500, Json.error(e.getMessage()));
      }
    });
    server.createContext("/api/addresses", ex -> {
      try {
        handleRequest(ex, new AddressController());
      } catch (Exception e) {
        HttpUtils.sendJson(ex, 500, Json.error(e.getMessage()));
      }
    });
    server.createContext("/api/clients", ex -> {
      try {
        handleRequest(ex, new ClientController());
      } catch (Exception e) {
        HttpUtils.sendJson(ex, 500, Json.error(e.getMessage()));
      }
    });
    server.createContext("/api/contracts", ex -> {
      try {
        handleRequest(ex, new ContractController());
      } catch (Exception e) {
        HttpUtils.sendJson(ex, 500, Json.error(e.getMessage()));
      }
    });
    server.createContext("/api/favorites", ex -> {
      try {
        handleRequest(ex, new FavoriteController());
      } catch (Exception e) {
        HttpUtils.sendJson(ex, 500, Json.error(e.getMessage()));
      }
    });
    server.createContext("/api/properties", ex -> {
      try {
        handleRequest(ex, new PropertyController());
      } catch (Exception e) {
        HttpUtils.sendJson(ex, 500, Json.error(e.getMessage()));
      }
    });
    server.createContext("/api/property_images", ex -> {
      try {
        handleRequest(ex, new PropertyImageController());
      } catch (Exception e) {
        HttpUtils.sendJson(ex, 500, Json.error(e.getMessage()));
      }
    });
    server.createContext("/api/transactions", ex -> {
      try {
        handleRequest(ex, new TransactionController());
      } catch (Exception e) {
        HttpUtils.sendJson(ex, 500, Json.error(e.getMessage()));
      }
    });
    server.createContext("/api/visits", ex -> {
      try {
        handleRequest(ex, new VisitController());
      } catch (Exception e) {
        HttpUtils.sendJson(ex, 500, Json.error(e.getMessage()));
      }
    });
    server.setExecutor(null);
    return server;
  }
}
