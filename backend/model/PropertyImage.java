package model;

public class PropertyImage {

  public int id;
  public int property_id;
  public String image_url;
  public boolean is_main;
  public String created_at;

  public PropertyImage() {
  }

  public PropertyImage(int id, int property_id, String image_url, boolean is_main, String created_at) {
    this.id = id;
    this.property_id = property_id;
    this.image_url = image_url;
    this.is_main = is_main;
    this.created_at = created_at;
  }
}
