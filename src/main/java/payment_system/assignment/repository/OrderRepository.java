package payment_system.assignment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import payment_system.assignment.domain.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
