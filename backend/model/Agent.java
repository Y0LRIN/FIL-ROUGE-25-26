package model;

public class Agent {

  public int id;
  public String name;
  public String email;
  public String phone;
  public boolean is_admin;
  public String created_at;

  public Agent() {
  }

  public Agent(int id, String name, String email, String phone, boolean is_admin, String created_at) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.phone = phone;
    this.is_admin = is_admin;
    this.created_at = created_at;
  }
}
