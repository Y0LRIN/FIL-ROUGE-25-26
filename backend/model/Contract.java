package model;

import model.enums.ContractType;

public class Contract {

  public int id;
  public int property_id;
  public int client_id;
  public int agent_id;
  public ContractType type;
  public String signed_at;

  public Contract() {
  }

  public Contract(int id, int property_id, int client_id, int agent_id, ContractType type, String signed_at) {
    this.id = id;
    this.property_id = property_id;
    this.client_id = client_id;
    this.agent_id = agent_id;
    this.type = type;
    this.signed_at = signed_at;
  }
}
