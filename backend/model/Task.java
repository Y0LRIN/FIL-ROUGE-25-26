package model;

public class Task {
  public int id;
  public int userId;
  public String title;
  public boolean done;

  public Task() {
  }

  public Task(int id, int userId, String title, boolean done) {
    this.id = id;
    this.userId = userId;
    this.title = title;
    this.done = done;
  }
}
