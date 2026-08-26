package com.example.payclear;

import jakarta.persistence.*;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String payerName;
    private Double amount;
    private String status;

    public Payment() {}

    public Payment(String payerName, Double amount, String status) {
        this.payerName = payerName;
        this.amount = amount;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPayerName() { return payerName; }
    public void setPayerName(String payerName) { this.payerName = payerName; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}