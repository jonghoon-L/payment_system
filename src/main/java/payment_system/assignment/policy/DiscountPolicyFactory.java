package payment_system.assignment.policy;

import org.springframework.stereotype.Component;
import payment_system.assignment.domain.MemberGrade;

import java.util.EnumMap;
import java.util.Map;

@Component
public class DiscountPolicyFactory {
    private final Map<MemberGrade, DiscountPolicy> policyMap;

    public DiscountPolicyFactory(NormalDiscountPolicy normalDiscountPolicy,
                                 VipDiscountPolicy vipDiscountPolicy,
                                 VvipDiscountPolicy vvipDiscountPolicy) {
        this.policyMap = new EnumMap<>(MemberGrade.class);
        this.policyMap.put(MemberGrade.NORMAL, normalDiscountPolicy);
        this.policyMap.put(MemberGrade.VIP, vipDiscountPolicy);
        this.policyMap.put(MemberGrade.VVIP, vvipDiscountPolicy);
    }

    public DiscountPolicy getPolicy(MemberGrade grade) {
        return policyMap.get(grade);
    }
}
