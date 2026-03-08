package payment_system.assignment.repository;

import org.springframework.stereotype.Repository;
import payment_system.assignment.domain.Payment;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class MemoryPaymentRepository implements PaymentRepository {
    private final Map<Long, Payment> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Payment save(Payment payment) {
        Long id = idGenerator.getAndIncrement();
        Payment savedPayment = new Payment(
                id,
                payment.getOrder(),
                payment.getFinalAmount(),
                payment.getPaymentMethod(),
                payment.getPaymentDateTime()
        );
        store.put(id, savedPayment);
        return savedPayment;
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }
}
