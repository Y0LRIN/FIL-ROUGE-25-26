package model;

public class Address {

  public int id;
  public String street;
  public String city;
  public int postal_code;
  public String country;

  public Address() {
  }

  public Address(int id, String street, String city, int postal_code, String country) {
    this.id = id;
    this.street = street;
    this.city = city;
    this.postal_code = postal_code;
    this.country = country;
  }
}
