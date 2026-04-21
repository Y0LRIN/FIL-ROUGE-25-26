package model;

public class Favorite {

  public int id;
  public int client_id;
  public int property_id;
  public String created_at;

  public Favorite() {
  }

  public Favorite(int id, int client_id, int property_id, String created_at) {
    this.id = id;
    this.client_id = client_id;
    this.property_id = property_id;
    this.created_at = created_at;
  }
}
