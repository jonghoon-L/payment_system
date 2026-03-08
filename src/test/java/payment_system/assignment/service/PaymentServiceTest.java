package payment_system.assignment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import payment_system.assignment.domain.Member;
import payment_system.assignment.domain.MemberGrade;
import payment_system.assignment.domain.Order;
import payment_system.assignment.domain.Payment;
import payment_system.assignment.domain.PaymentMethod;
import payment_system.assignment.repository.MemberRepository;
import payment_system.assignment.repository.OrderRepository;
import payment_system.assignment.repository.PaymentDiscountHistoryRepository;
import payment_system.assignment.repository.PaymentRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private PaymentDiscountHistoryRepository paymentDiscountHistoryRepository;

    private Member vipMember;
    private Member vvipMember;
    private Member normalMember;

    @BeforeEach
    void setUp() {
        vipMember = memberRepository.save(new Member("VIP회원", MemberGrade.VIP));
        vvipMember = memberRepository.save(new Member("VVIP회원", MemberGrade.VVIP));
        normalMember = memberRepository.save(new Member("일반회원", MemberGrade.NORMAL));
    }

    @Test
    @DisplayName("VIP 회원이 10,000원 상품을 신용카드로 결제할 때 등급할인만 적용되어 최종 금액은 9,000원이고 결제 수단은 CREDIT_CARD로 저장된다")
    void vip회원_10000원_신용카드_결제_9000원_할인적용() {
        // given
        Order order = orderRepository.save(new Order("상품A", 10_000, vipMember));
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;

        // when
        Payment payment = paymentService.processPayment(order.getId(), paymentMethod);

        // then
        assertThat(payment.getFinalAmount()).isEqualTo(9_000);
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(payment.getOrder().getId()).isEqualTo(order.getId());
        assertThat(payment.getPaymentDateTime()).isNotNull();
        assertThat(payment.getId()).isNotNull();

        Payment found = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(found.getFinalAmount()).isEqualTo(9_000);
        assertThat(found.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
    }

    @Test
    @DisplayName("VVIP 회원이 20,000원 상품을 포인트로 결제할 때 10% 등급할인 후 5% 중복할인 적용되어 최종 금액은 17,100원이다")
    void vvip회원_20000원_포인트_결제_중복할인_적용() {
        // given
        Order order = orderRepository.save(new Order("상품B", 20_000, vvipMember));
        PaymentMethod paymentMethod = PaymentMethod.POINT;

        // when
        Payment payment = paymentService.processPayment(order.getId(), paymentMethod);

        // then
        assertThat(payment.getFinalAmount()).isEqualTo(17_100);
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.POINT);
        assertThat(payment.getOrder().getId()).isEqualTo(order.getId());
        assertThat(payment.getPaymentDateTime()).isNotNull();

        Payment found = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(found.getFinalAmount()).isEqualTo(17_100);
    }

    @Test
    @DisplayName("NORMAL 회원이 10,000원 상품 결제 시 할인 없이 최종 금액 10,000원이 산출된다")
    void normal회원_10000원_결제_할인없음() {
        // given
        Order order = orderRepository.save(new Order("상품C", 10_000, normalMember));
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;

        // when
        Payment payment = paymentService.processPayment(order.getId(), paymentMethod);

        // then
        assertThat(payment.getFinalAmount()).isEqualTo(10_000);
        assertThat(payment.getOrder().getId()).isEqualTo(order.getId());
    }

    @Test
    @DisplayName("VIP 회원이 포인트 결제 시 등급할인 1,000원 적용 후 5% 중복할인 적용된다")
    void vip회원_포인트결제_등급할인_중복할인_적용() {
        // given
        Order order = orderRepository.save(new Order("검증상품", 5_000, vipMember));

        // when
        Payment payment = paymentService.processPayment(order.getId(), PaymentMethod.POINT);

        // then
        assertThat(payment.getOrder().getItemName()).isEqualTo("검증상품");
        assertThat(payment.getOrder().getOriginalPrice()).isEqualTo(5_000);
        assertThat(payment.getFinalAmount()).isEqualTo(3_800);
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.POINT);
        assertThat(payment.getPaymentDateTime()).isNotNull();
    }

    @Test
    @DisplayName("결제 완료 후 PaymentRepository에 저장되어 findById로 조회 가능하다")
    void processPayment_저장후_조회_가능() {
        // given
        Order order = orderRepository.save(new Order("저장검증상품", 10_000, vvipMember));

        // when
        Payment saved = paymentService.processPayment(order.getId(), PaymentMethod.CREDIT_CARD);

        // then
        assertThat(paymentRepository.findById(saved.getId())).isPresent();
        Payment retrieved = paymentRepository.findById(saved.getId()).orElseThrow();
        assertThat(retrieved.getId()).isEqualTo(saved.getId());
        assertThat(retrieved.getFinalAmount()).isEqualTo(saved.getFinalAmount());
    }

    @Test
    @DisplayName("결제 시 적용된 할인 정책이 PaymentDiscountHistory에 저장된다")
    void processPayment_할인이력_저장() {
        // given
        Order order = orderRepository.save(new Order("이력검증상품", 10_000, vvipMember));

        // when
        Payment payment = paymentService.processPayment(order.getId(), PaymentMethod.POINT);

        // then
        long historyCount = paymentDiscountHistoryRepository.count();
        assertThat(historyCount).isEqualTo(2);
    }
}
