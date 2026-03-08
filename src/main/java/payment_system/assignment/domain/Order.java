package payment_system.assignment.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Order {
    private final Long id;
    private final String itemName;
    private final int originalPrice;
    private final Member member;
}
