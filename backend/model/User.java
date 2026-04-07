package model;

public class User {
  public int id;
  public String name;
  public String email;
  public String createdAt;

  public User() {
  }

  public User(int id, String name, String email, String createdAt) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.createdAt = createdAt;
  }
}
