package payment_system.assignment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import payment_system.assignment.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
