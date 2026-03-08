package payment_system.assignment.service;

import jakarta.persistence.EntityManager;
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
import payment_system.assignment.domain.PaymentDiscountHistory;
import payment_system.assignment.domain.PaymentMethod;
import payment_system.assignment.repository.MemberRepository;
import payment_system.assignment.repository.OrderRepository;
import payment_system.assignment.repository.PaymentDiscountHistoryRepository;
import payment_system.assignment.repository.PaymentRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentIntegrationTest {

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
    @Autowired
    private EntityManager entityManager;

    private Member vvipMember;
    private Member vipMember;

    @BeforeEach
    void setUp() {
        vvipMember = memberRepository.save(new Member("VVIP회원", MemberGrade.VVIP));
        vipMember = memberRepository.save(new Member("VIP회원", MemberGrade.VIP));
    }

    @Test
    @DisplayName("VVIP 회원이 10,000원 상품을 POINT로 결제 시 1차 등급할인 1,000원 적용 후 2차 포인트 5% 할인 450원이 중복 적용되어 최종 8,550원이 된다")
    void vvip_10000원_포인트_결제_중복할인_8550원() {
        // given
        Order order = orderRepository.save(new Order("테스트상품", 10_000, vvipMember));
        PaymentMethod paymentMethod = PaymentMethod.POINT;

        // when
        Payment payment = paymentService.processPayment(order.getId(), paymentMethod);

        // then
        assertThat(payment.getFinalAmount()).isEqualTo(8_550);
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.POINT);
        assertThat(payment.getOrder().getOriginalPrice()).isEqualTo(10_000);

        List<PaymentDiscountHistory> histories = paymentDiscountHistoryRepository.findByPayment_IdOrderByIdAsc(payment.getId());
        assertThat(histories).hasSize(2);

        PaymentDiscountHistory gradeHistory = histories.get(0);
        assertThat(gradeHistory.getPolicyName()).isEqualTo("VvipDiscountPolicy");
        assertThat(gradeHistory.getMemberGrade()).isEqualTo("VVIP");
        assertThat(gradeHistory.getDiscountRate()).isEqualTo(10);
        assertThat(gradeHistory.getDiscountAmount()).isEqualTo(1_000);

        PaymentDiscountHistory pointHistory = histories.get(1);
        assertThat(pointHistory.getPolicyName()).isEqualTo("PointDuplicateDiscountPolicy");
        assertThat(pointHistory.getDiscountRate()).isEqualTo(5);
        assertThat(pointHistory.getDiscountAmount()).isEqualTo(450);
    }

    @Test
    @DisplayName("VIP 회원 10,000원 CREDIT_CARD 결제 시 1,000원 고정 할인 적용되어 최종 9,000원이며, 정책 변경 후에도 DB 재조회 시 과거 데이터 정합성이 유지된다")
    void vip_결제후_정책변경_과거데이터_정합성_유지() {
        // given
        Order order = orderRepository.save(new Order("정합성검증상품", 10_000, vipMember));

        // when
        Payment payment = paymentService.processPayment(order.getId(), PaymentMethod.CREDIT_CARD);

        // then
        assertThat(payment.getFinalAmount()).isEqualTo(9_000);

        List<PaymentDiscountHistory> histories = paymentDiscountHistoryRepository.findByPayment_IdOrderByIdAsc(payment.getId());
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getDiscountAmount()).isEqualTo(1_000);
        assertThat(histories.get(0).getPolicyName()).isEqualTo("VipDiscountPolicy");

        entityManager.flush();
        entityManager.clear();

        Payment refetchedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(refetchedPayment.getFinalAmount()).isEqualTo(9_000);

        List<PaymentDiscountHistory> refetchedHistories = paymentDiscountHistoryRepository.findByPayment_IdOrderByIdAsc(refetchedPayment.getId());
        assertThat(refetchedHistories).hasSize(1);
        assertThat(refetchedHistories.get(0).getDiscountAmount()).isEqualTo(1_000);
    }
}
