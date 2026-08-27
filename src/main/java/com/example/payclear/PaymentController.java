package com.example.payclear;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @PostMapping
    public Payment createPayment(@RequestBody Payment payment) {
        if (payment.getStatus() == null || payment.getStatus().isBlank()) {
            payment.setStatus("SUCCESS");
        }
        if (payment.getPaymentMethod() == null || payment.getPaymentMethod().isBlank()) {
            payment.setPaymentMethod("UPI");
        }
        if (payment.getPayoutStatus() == null || payment.getPayoutStatus().isBlank()) {
            payment.setPayoutStatus("UNSETTLED");
        }

        payment.setTransactionDate(LocalDateTime.now());
        payment.calculateFees();

        return paymentRepository.save(payment);
    }

    @GetMapping
    public List<Payment> getAllPayments(@RequestParam(required = false) String search) {
        if (search != null && !search.isBlank()) {
            return paymentRepository.findByPayerNameContainingIgnoreCase(search);
        }
        return paymentRepository.findAll();
    }

    @GetMapping("/{id}")
    public Payment getPaymentById(@PathVariable Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Payment> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return paymentRepository.findById(id).map(payment -> {
            if (body.containsKey("status")) {
                payment.setStatus(body.get("status"));
            }
            if (body.containsKey("payoutStatus")) {
                payment.setPayoutStatus(body.get("payoutStatus"));
            }
            return ResponseEntity.ok(paymentRepository.save(payment));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public String deletePayment(@PathVariable Long id) {
        paymentRepository.deleteById(id);
        return "Payment with ID " + id + " has been deleted successfully.";
    }
}