package payment_system.assignment.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class Payment {
    private final Long id;
    private final Order order;
    private final int finalAmount;
    private final PaymentMethod paymentMethod;
    private final LocalDateTime paymentDateTime;
}
