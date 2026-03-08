package payment_system.assignment.policy;

import org.springframework.stereotype.Component;
import payment_system.assignment.domain.Member;

@Component
public class VipDiscountPolicy implements DiscountPolicy {
    private static final int VIP_DISCOUNT_AMOUNT = 1000;

    @Override
    public int calculateDiscountAmount(Member member, int originalPrice) {
        return Math.min(originalPrice, VIP_DISCOUNT_AMOUNT);
    }
}
