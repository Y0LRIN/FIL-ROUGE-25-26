package model;

public class Visit {

  public int id;
  public int property_id;
  public int client_id;
  public int agent_id;
  public String visit_date;
  public String feedback;

  public Visit() {
  }

  public Visit(int id, int property_id, int client_id, int agent_id, String visit_date, String feedback) {
    this.id = id;
    this.property_id = property_id;
    this.client_id = client_id;
    this.agent_id = agent_id;
    this.visit_date = visit_date;
    this.feedback = feedback;
  }
}
