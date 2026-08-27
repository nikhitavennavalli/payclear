package com.example.payclear;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Find all transactions by payer name
    List<Payment> findByPayerName(String payerName);

    // Find all successful or failed transactions
    List<Payment> findByStatus(String status);
}