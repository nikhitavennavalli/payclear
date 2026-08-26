package com.example.payclear;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @PostMapping
    public Payment createPayment(@RequestBody Payment payment) {
        payment.setStatus("SUCCESS");
        return paymentRepository.save(payment);
    }

    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @GetMapping("/{id}")
    public Payment getPaymentById(@PathVariable Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
    }

    @DeleteMapping("/{id}")
    public String deletePayment(@PathVariable Long id) {
        paymentRepository.deleteById(id);
        return "Payment with ID " + id + " has been deleted successfully.";
    }
}