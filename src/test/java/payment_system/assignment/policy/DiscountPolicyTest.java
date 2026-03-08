package payment_system.assignment.policy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import payment_system.assignment.domain.Member;
import payment_system.assignment.domain.MemberGrade;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountPolicyTest {

    private final NormalDiscountPolicy normalDiscountPolicy = new NormalDiscountPolicy();
    private final VipDiscountPolicy vipDiscountPolicy = new VipDiscountPolicy();
    private final VvipDiscountPolicy vvipDiscountPolicy = new VvipDiscountPolicy();
    private final DiscountPolicyFactory discountPolicyFactory = new DiscountPolicyFactory(
            normalDiscountPolicy, vipDiscountPolicy, vvipDiscountPolicy);

    @Test
    @DisplayName("NORMAL 등급 회원이 10,000원 주문 시 할인 금액은 0원이어야 한다")
    void normalGradeMember_10000원_주문_할인_0원() {
        // given
        Member member = new Member(1L, "일반회원", MemberGrade.NORMAL);
        int originalPrice = 10_000;

        // when
        int discountAmount = normalDiscountPolicy.calculateDiscountAmount(member, originalPrice);

        // then
        assertThat(discountAmount).isZero();
    }

    @Test
    @DisplayName("VIP 등급 회원이 10,000원 주문 시 정확히 1,000원이 할인되어야 한다")
    void vipGradeMember_10000원_주문_1000원_할인() {
        // given
        Member member = new Member(1L, "VIP회원", MemberGrade.VIP);
        int originalPrice = 10_000;

        // when
        int discountAmount = vipDiscountPolicy.calculateDiscountAmount(member, originalPrice);

        // then
        assertThat(discountAmount).isEqualTo(1_000);
    }

    @Test
    @DisplayName("VIP 등급 회원이 800원(1,000원 미만) 주문 시 원가인 800원만 할인되어야 한다")
    void vipGradeMember_800원_주문_원가만큼_할인() {
        // given
        Member member = new Member(1L, "VIP회원", MemberGrade.VIP);
        int originalPrice = 800;

        // when
        int discountAmount = vipDiscountPolicy.calculateDiscountAmount(member, originalPrice);

        // then
        assertThat(discountAmount).isEqualTo(800);
        assertThat(discountAmount).isNotNegative();
    }

    @Test
    @DisplayName("VVIP 등급 회원이 10,000원 주문 시 10%인 1,000원이 할인되어야 한다")
    void vvipGradeMember_10000원_주문_10퍼센트_할인() {
        // given
        Member member = new Member(1L, "VVIP회원", MemberGrade.VVIP);
        int originalPrice = 10_000;

        // when
        int discountAmount = vvipDiscountPolicy.calculateDiscountAmount(member, originalPrice);

        // then
        assertThat(discountAmount).isEqualTo(1_000);
    }

    @ParameterizedTest
    @EnumSource(MemberGrade.class)
    @DisplayName("DiscountPolicyFactory에 각 MemberGrade를 전달했을 때 알맞은 정책 구현체가 반환된다")
    void factory_각_MemberGrade별_올바른_정책_반환(MemberGrade grade) {
        // when
        DiscountPolicy policy = discountPolicyFactory.getPolicy(grade);

        // then
        assertThat(policy).isNotNull();
        switch (grade) {
            case NORMAL -> assertThat(policy).isInstanceOf(NormalDiscountPolicy.class);
            case VIP -> assertThat(policy).isInstanceOf(VipDiscountPolicy.class);
            case VVIP -> assertThat(policy).isInstanceOf(VvipDiscountPolicy.class);
        }
    }
}
