package com.example.payclear;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @PostMapping
    public Payment createPayment(@RequestBody Payment payment) {
        // Input Guardrails: Validate Payer Name and Amount
        if (payment.getPayerName() == null || payment.getPayerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Payer name cannot be empty.");
        }
        if (payment.getAmount() == null || payment.getAmount() <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }

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

    @GetMapping("/reconcile")
    public ReconciliationSummary getReconciliationSummary() {
        List<Payment> allPayments = paymentRepository.findAll();

        double totalGross = 0.0;
        double totalFees = 0.0;
        double totalGst = 0.0;
        double expectedPayout = 0.0;
        long successfulCount = 0;
        long pendingCount = 0;

        for (Payment p : allPayments) {
            if ("SUCCESS".equalsIgnoreCase(p.getStatus())) {
                totalGross += (p.getAmount() != null) ? p.getAmount() : 0.0;
                totalFees += (p.getProcessingFee() != null) ? p.getProcessingFee() : 0.0;
                totalGst += (p.getGst() != null) ? p.getGst() : 0.0;
                expectedPayout += (p.getNetAmount() != null) ? p.getNetAmount() : 0.0;
                successfulCount++;
            } else if ("PENDING".equalsIgnoreCase(p.getStatus())) {
                pendingCount++;
            }
        }

        totalGross = Math.round(totalGross * 100.0) / 100.0;
        totalFees = Math.round(totalFees * 100.0) / 100.0;
        totalGst = Math.round(totalGst * 100.0) / 100.0;
        expectedPayout = Math.round(expectedPayout * 100.0) / 100.0;

        return new ReconciliationSummary(
                totalGross, totalFees, totalGst, expectedPayout,
                (long) allPayments.size(), successfulCount, pendingCount
        );
    }

    @PostMapping("/ai-query")
    public ResponseEntity<?> processAiQuery(@RequestBody AiQueryRequest request) {
        if (request.getQuery() == null || request.getQuery().isBlank()) {
            return ResponseEntity.badRequest().body("Query cannot be empty.");
        }

        String prompt = request.getQuery().toLowerCase();
        List<Payment> all = paymentRepository.findAll();

        String targetStatus = null;
        String targetMethod = null;
        Double maxAmount = null;
        Double minAmount = null;
        String targetPayer = null;

        // 1. Detect Status (handles successfull, succesfull, sucsses, etc.)
        if (prompt.contains("succ") || prompt.contains("sucs") || prompt.contains("sces")) {
            targetStatus = "SUCCESS";
        } else if (prompt.contains("pend") || prompt.contains("pnd")) {
            targetStatus = "PENDING";
        } else if (prompt.contains("refun") || prompt.contains("rfnd")) {
            targetStatus = "REFUNDED";
        }

        // 2. Detect Payment Methods (handles card, crdit, upi, netbanking)
        if (prompt.contains("upi")) {
            targetMethod = "UPI";
        } else if (prompt.contains("card") || prompt.contains("crd") || prompt.contains("debit") || prompt.contains("credit")) {
            targetMethod = "CARD";
        } else if (prompt.contains("bank") || prompt.contains("net")) {
            targetMethod = "NET_BANKING";
        }

        // 3. Detect Amount Operators and Numbers
        Pattern numberPattern = Pattern.compile("(below|under|less|belw|undr|<|above|over|greater|abv|ovr|>)?\\s*(\\d+(\\.\\d+)?)");
        Matcher matcher = numberPattern.matcher(prompt);

        while (matcher.find()) {
            String operator = matcher.group(1);
            double val = Double.parseDouble(matcher.group(2));

            if (operator != null) {
                if (operator.matches("below|under|less|belw|undr|<")) {
                    maxAmount = val;
                } else if (operator.matches("above|over|greater|abv|ovr|>")) {
                    minAmount = val;
                }
            } else {
                // Default fallback if no operator specified before amount
                if (prompt.contains("above") || prompt.contains("over") || prompt.contains("abv") || prompt.contains(">")) {
                    minAmount = val;
                } else if (prompt.contains("below") || prompt.contains("under") || prompt.contains("belw") || prompt.contains("<")) {
                    maxAmount = val;
                }
            }
        }

        // 4. Token-Based Payer Name Filtering (Strips common non-name terms regardless of spelling)
        String[] tokens = prompt.split("\\s+");
        StringBuilder payerNameBuilder = new StringBuilder();

        for (String token : tokens) {
            String cleanToken = token.replaceAll("[^a-zA-Z]", "");
            if (cleanToken.isEmpty()) continue;

            // Ignore system keywords and their common misspellings
            boolean isSystemKeyword = cleanToken.matches("(?i)^(show|get|list|pay|pmnt|payment|payments|trans|trancation|transaction|transactions|transacation|success|successful|successfull|succesfull|pending|refunded|below|under|above|over|less|greater|upi|card|credit|debit|net|bank|banking)$");

            if (!isSystemKeyword) {
                if (payerNameBuilder.length() > 0) payerNameBuilder.append(" ");
                payerNameBuilder.append(cleanToken);
            }
        }

        if (payerNameBuilder.length() > 0) {
            targetPayer = payerNameBuilder.toString();
        }

        // Apply Filtering
        final String fStatus = targetStatus;
        final String fMethod = targetMethod;
        final Double fMax = maxAmount;
        final Double fMin = minAmount;
        final String fPayer = targetPayer;

        List<Payment> filtered = all.stream().filter(p -> {
            boolean match = true;
            if (fStatus != null && !fStatus.equalsIgnoreCase(p.getStatus())) match = false;
            if (fMethod != null && !fMethod.equalsIgnoreCase(p.getPaymentMethod())) match = false;
            if (fMax != null && (p.getAmount() == null || p.getAmount() >= fMax)) match = false;
            if (fMin != null && (p.getAmount() == null || p.getAmount() <= fMin)) match = false;
            if (fPayer != null && (p.getPayerName() == null || !p.getPayerName().toLowerCase().contains(fPayer))) match = false;
            return match;
        }).collect(Collectors.toList());

        // Construct Generated SQL Output
        StringBuilder sql = new StringBuilder("SELECT * FROM payments WHERE 1=1");
        if (fStatus != null) sql.append(" AND status = '").append(fStatus).append("'");
        if (fMethod != null) sql.append(" AND payment_method = '").append(fMethod).append("'");
        if (fMax != null) sql.append(" AND amount < ").append(fMax);
        if (fMin != null) sql.append(" AND amount > ").append(fMin);
        if (fPayer != null) sql.append(" AND LOWER(payer_name) LIKE '%").append(fPayer).append("%'");
        sql.append(";");

        double totalSum = filtered.stream().mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0).sum();

        return ResponseEntity.ok(Map.of(
                "sql", sql.toString(),
                "summary", "Found " + filtered.size() + " matching record(s) totaling $" + String.format("%.2f", totalSum) + ".",
                "data", filtered
        ));
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