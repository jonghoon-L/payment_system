package payment_system.assignment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import payment_system.assignment.domain.Member;
import payment_system.assignment.domain.MemberGrade;
import payment_system.assignment.domain.Order;
import payment_system.assignment.domain.Payment;
import payment_system.assignment.domain.PaymentMethod;
import payment_system.assignment.policy.DiscountPolicyFactory;
import payment_system.assignment.policy.NormalDiscountPolicy;
import payment_system.assignment.policy.VipDiscountPolicy;
import payment_system.assignment.policy.VvipDiscountPolicy;
import payment_system.assignment.repository.MemoryPaymentRepository;
import payment_system.assignment.repository.PaymentRepository;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentServiceTest {

    private PaymentService paymentService;
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        DiscountPolicyFactory discountPolicyFactory = new DiscountPolicyFactory(
                new NormalDiscountPolicy(),
                new VipDiscountPolicy(),
                new VvipDiscountPolicy()
        );
        paymentRepository = new MemoryPaymentRepository();
        paymentService = new PaymentService(discountPolicyFactory, paymentRepository);
    }

    @Test
    @DisplayName("VIP 회원이 10,000원 상품을 신용카드로 결제할 때 최종 금액은 9,000원이고 결제 수단은 CREDIT_CARD로 저장된다")
    void vip회원_10000원_신용카드_결제_9000원_할인적용() {
        // given
        Member member = new Member(1L, "VIP회원", MemberGrade.VIP);
        Order order = new Order(1L, "상품A", 10_000, member);
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;

        // when
        Payment payment = paymentService.processPayment(order, paymentMethod);

        // then
        assertThat(payment.getFinalAmount()).isEqualTo(9_000);
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(payment.getOrder()).isEqualTo(order);
        assertThat(payment.getPaymentDateTime()).isNotNull();
        assertThat(payment.getId()).isNotNull();

        Payment found = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(found.getFinalAmount()).isEqualTo(9_000);
        assertThat(found.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
    }

    @Test
    @DisplayName("VVIP 회원이 20,000원 상품을 포인트로 결제할 때 10% 할인 적용되어 최종 금액은 18,000원이고 결제 수단은 POINT로 저장된다")
    void vvip회원_20000원_포인트_결제_10퍼센트_할인적용() {
        // given
        Member member = new Member(1L, "VVIP회원", MemberGrade.VVIP);
        Order order = new Order(1L, "상품B", 20_000, member);
        PaymentMethod paymentMethod = PaymentMethod.POINT;

        // when
        Payment payment = paymentService.processPayment(order, paymentMethod);

        // then
        assertThat(payment.getFinalAmount()).isEqualTo(18_000);
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.POINT);
        assertThat(payment.getOrder()).isEqualTo(order);
        assertThat(payment.getPaymentDateTime()).isNotNull();
        assertThat(payment.getId()).isNotNull();

        Payment found = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(found.getFinalAmount()).isEqualTo(18_000);
        assertThat(found.getPaymentMethod()).isEqualTo(PaymentMethod.POINT);
    }

    @Test
    @DisplayName("NORMAL 회원이 10,000원 상품 결제 시 할인 없이 최종 금액 10,000원이 산출된다")
    void normal회원_10000원_결제_할인없음() {
        // given
        Member member = new Member(1L, "일반회원", MemberGrade.NORMAL);
        Order order = new Order(1L, "상품C", 10_000, member);
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;

        // when
        Payment payment = paymentService.processPayment(order, paymentMethod);

        // then
        assertThat(payment.getFinalAmount()).isEqualTo(10_000);
        assertThat(payment.getOrder()).isEqualTo(order);
    }

    @Test
    @DisplayName("반환된 Payment 객체에 주문 정보, 최종 금액, 결제 수단, 결제 일시가 모두 기록된다")
    void processPayment_반환값_필드_검증() {
        // given
        Member member = new Member(1L, "VIP회원", MemberGrade.VIP);
        Order order = new Order(1L, "검증상품", 5_000, member);

        // when
        Payment payment = paymentService.processPayment(order, PaymentMethod.POINT);

        // then
        assertThat(payment.getOrder()).isSameAs(order);
        assertThat(payment.getOrder().getItemName()).isEqualTo("검증상품");
        assertThat(payment.getOrder().getOriginalPrice()).isEqualTo(5_000);
        assertThat(payment.getFinalAmount()).isEqualTo(4_000);
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.POINT);
        assertThat(payment.getPaymentDateTime()).isNotNull();
    }

    @Test
    @DisplayName("결제 완료 후 PaymentRepository에 저장되어 findById로 조회 가능하다")
    void processPayment_저장후_조회_가능() {
        // given
        Member member = new Member(1L, "VVIP회원", MemberGrade.VVIP);
        Order order = new Order(1L, "저장검증상품", 10_000, member);

        // when
        Payment saved = paymentService.processPayment(order, PaymentMethod.CREDIT_CARD);

        // then
        assertThat(paymentRepository.findById(saved.getId())).isPresent();
        Payment retrieved = paymentRepository.findById(saved.getId()).orElseThrow();
        assertThat(retrieved.getId()).isEqualTo(saved.getId());
        assertThat(retrieved.getFinalAmount()).isEqualTo(saved.getFinalAmount());
        assertThat(retrieved.getOrder()).isEqualTo(saved.getOrder());
    }
}
