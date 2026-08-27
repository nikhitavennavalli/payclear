package com.example.payclear;

public class ReconciliationSummary {
    private Double totalGrossAmount;
    private Double totalProcessingFees;
    private Double totalGstDeductions;
    private Double expectedBankPayout;
    private Long totalTransactions;
    private Long successfulTransactions;
    private Long pendingTransactions;

    public ReconciliationSummary(Double totalGrossAmount, Double totalProcessingFees, Double totalGstDeductions,
                                 Double expectedBankPayout, Long totalTransactions, Long successfulTransactions,
                                 Long pendingTransactions) {
        this.totalGrossAmount = totalGrossAmount;
        this.totalProcessingFees = totalProcessingFees;
        this.totalGstDeductions = totalGstDeductions;
        this.expectedBankPayout = expectedBankPayout;
        this.totalTransactions = totalTransactions;
        this.successfulTransactions = successfulTransactions;
        this.pendingTransactions = pendingTransactions;
    }

    // Getters
    public Double getTotalGrossAmount() { return totalGrossAmount; }
    public Double getTotalProcessingFees() { return totalProcessingFees; }
    public Double getTotalGstDeductions() { return totalGstDeductions; }
    public Double getExpectedBankPayout() { return expectedBankPayout; }
    public Long getTotalTransactions() { return totalTransactions; }
    public Long getSuccessfulTransactions() { return successfulTransactions; }
    public Long getPendingTransactions() { return pendingTransactions; }
}