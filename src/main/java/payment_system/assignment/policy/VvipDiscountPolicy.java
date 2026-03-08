package payment_system.assignment.policy;

import org.springframework.stereotype.Component;
import payment_system.assignment.domain.Member;

@Component
public class VvipDiscountPolicy implements DiscountPolicy {
    private static final double VVIP_DISCOUNT_RATE = 0.1;

    @Override
    public int calculateDiscountAmount(Member member, int originalPrice) {
        return (int) (originalPrice * VVIP_DISCOUNT_RATE);
    }
}
