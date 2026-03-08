package payment_system.assignment.policy;

import payment_system.assignment.domain.Member;

public interface DiscountPolicy {
    int calculateDiscountAmount(Member member, int originalPrice);
}
