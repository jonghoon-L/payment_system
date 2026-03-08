package payment_system.assignment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import payment_system.assignment.domain.PaymentDiscountHistory;

import java.util.List;

public interface PaymentDiscountHistoryRepository extends JpaRepository<PaymentDiscountHistory, Long> {
    List<PaymentDiscountHistory> findByPayment_IdOrderByIdAsc(Long paymentId);
}
