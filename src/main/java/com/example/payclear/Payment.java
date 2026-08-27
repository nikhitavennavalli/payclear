package com.example.payclear;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String payerName;
    private Double amount;
    private String status; // SUCCESS, PENDING, FAILED, REFUNDED

    private String paymentMethod; // UPI, CARD, NET_BANKING
    private Double processingFee;
    private Double gst;
    private Double netAmount;

    private String payoutStatus; // SETTLED, UNSETTLED
    private LocalDateTime transactionDate;

    public Payment() {}

    public Payment(String payerName, Double amount, String status, String paymentMethod) {
        this.payerName = payerName;
        this.amount = amount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.transactionDate = LocalDateTime.now();
        calculateFees();
    }

    public void calculateFees() {
        if (this.amount != null && this.amount > 0) {
            // Standard rates: 2% processing fee, 18% GST on fee
            this.processingFee = Math.round((this.amount * 0.02) * 100.0) / 100.0;
            this.gst = Math.round((this.processingFee * 0.18) * 100.0) / 100.0;
            this.netAmount = Math.round((this.amount - this.processingFee - this.gst) * 100.0) / 100.0;
        } else {
            this.processingFee = 0.0;
            this.gst = 0.0;
            this.netAmount = 0.0;
        }
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

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public Double getProcessingFee() { return processingFee; }
    public void setProcessingFee(Double processingFee) { this.processingFee = processingFee; }

    public Double getGst() { return gst; }
    public void setGst(Double gst) { this.gst = gst; }

    public Double getNetAmount() { return netAmount; }
    public void setNetAmount(Double netAmount) { this.netAmount = netAmount; }

    public String getPayoutStatus() { return payoutStatus; }
    public void setPayoutStatus(String payoutStatus) { this.payoutStatus = payoutStatus; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
}