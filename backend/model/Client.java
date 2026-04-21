package model;

import model.enums.ClientType;

public class Client {

  public int id;
  public String first_name;
  public String last_name;
  public String email;
  public String phone;
  public ClientType type;

  public Client() {
  }

  public Client(int id, String first_name, String last_name, String email, String phone, ClientType type) {
    this.id = id;
    this.first_name = first_name;
    this.last_name = last_name;
    this.email = email;
    this.phone = phone;
    this.type = type;
  }
}
