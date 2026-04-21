package model;

import model.enums.PropertyStatus;
import model.enums.PropertyType;

public class Property {
  public int id;
  public String title;
  public String description;
  public int price;
  public float surface;
  public int rooms;
  public PropertyType type;
  public PropertyStatus status;
  public int agent_id;
  public int address_id;
  public String created_at;

  public Property() {
  }

  public Property(
      int id,
      String title,
      String description,
      int price,
      float surface,
      int rooms,
      PropertyType type,
      PropertyStatus status,
      int agent_id,
      int address_id,
      String created_at) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.price = price;
    this.surface = surface;
    this.rooms = rooms;
    this.type = type;
    this.status = status;
    this.agent_id = agent_id;
    this.address_id = address_id;
    this.created_at = created_at;
  }
}
