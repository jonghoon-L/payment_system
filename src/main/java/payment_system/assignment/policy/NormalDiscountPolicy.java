package payment_system.assignment.policy;

import org.springframework.stereotype.Component;
import payment_system.assignment.domain.Member;

@Component
public class NormalDiscountPolicy implements DiscountPolicy {
    @Override
    public int calculateDiscountAmount(Member member, int originalPrice) {
        return 0;
    }
}
