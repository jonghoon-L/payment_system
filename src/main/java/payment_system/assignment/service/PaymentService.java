package payment_system.assignment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import payment_system.assignment.domain.*;
import payment_system.assignment.policy.DiscountPolicy;
import payment_system.assignment.policy.DiscountPolicyFactory;
import payment_system.assignment.repository.OrderRepository;
import payment_system.assignment.repository.PaymentDiscountHistoryRepository;
import payment_system.assignment.repository.PaymentRepository;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    private static final double POINT_DUPLICATE_DISCOUNT_RATE = 0.05;
    private static final String POINT_DUPLICATE_POLICY_NAME = "PointDuplicateDiscountPolicy";

    private final DiscountPolicyFactory discountPolicyFactory;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentDiscountHistoryRepository paymentDiscountHistoryRepository;

    public PaymentService(DiscountPolicyFactory discountPolicyFactory,
                          OrderRepository orderRepository,
                          PaymentRepository paymentRepository,
                          PaymentDiscountHistoryRepository paymentDiscountHistoryRepository) {
        this.discountPolicyFactory = discountPolicyFactory;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.paymentDiscountHistoryRepository = paymentDiscountHistoryRepository;
    }

    @Transactional
    public Payment processPayment(Long orderId, PaymentMethod paymentMethod) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        Member member = order.getMember();
        int originalPrice = order.getOriginalPrice();

        DiscountPolicy gradePolicy = discountPolicyFactory.getPolicy(member.getGrade());
        int gradeDiscountAmount = gradePolicy.calculateDiscountAmount(member, originalPrice);
        int amountAfterGradeDiscount = originalPrice - gradeDiscountAmount;

        int pointDiscountAmount = 0;
        if (paymentMethod == PaymentMethod.POINT) {
            pointDiscountAmount = (int) (amountAfterGradeDiscount * POINT_DUPLICATE_DISCOUNT_RATE);
        }

        int finalAmount = amountAfterGradeDiscount - pointDiscountAmount;

        Payment payment = new Payment(order, finalAmount, paymentMethod, LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);

        saveGradeDiscountHistory(savedPayment, member.getGrade().name(), gradePolicy, gradeDiscountAmount);
        if (pointDiscountAmount > 0) {
            savePointDiscountHistory(savedPayment, pointDiscountAmount);
        }

        return savedPayment;
    }

    private void saveGradeDiscountHistory(Payment payment, String memberGrade, DiscountPolicy policy, int discountAmount) {
        int discountRate = memberGrade.equals(MemberGrade.VVIP.name()) ? 10 : 0;
        PaymentDiscountHistory history = new PaymentDiscountHistory(
                payment,
                memberGrade,
                policy.getClass().getSimpleName(),
                discountRate,
                discountAmount
        );
        paymentDiscountHistoryRepository.save(history);
    }

    private void savePointDiscountHistory(Payment payment, int discountAmount) {
        PaymentDiscountHistory history = new PaymentDiscountHistory(
                payment,
                payment.getOrder().getMember().getGrade().name(),
                POINT_DUPLICATE_POLICY_NAME,
                5,
                discountAmount
        );
        paymentDiscountHistoryRepository.save(history);
    }
}
