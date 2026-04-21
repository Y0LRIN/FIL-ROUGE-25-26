package model;

import model.enums.TransactionStatus;

public class Transaction {

  public int id;
  public int contract_id;
  public int amount;
  public String payment_date;
  public String payment_method;
  public TransactionStatus status;

  public Transaction() {
  }

  public Transaction(int id, int contract_id, int amount, String payment_date, String payment_method,
      TransactionStatus status) {
    this.id = id;
    this.contract_id = contract_id;
    this.amount = amount;
    this.payment_date = payment_date;
    this.payment_method = payment_method;
    this.status = status;
  }
}
