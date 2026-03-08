package payment_system.assignment.repository;

import payment_system.assignment.domain.Payment;

import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findById(Long id);
}
