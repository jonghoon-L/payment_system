package payment_system.assignment.service;

import org.springframework.stereotype.Service;
import payment_system.assignment.domain.Member;
import payment_system.assignment.domain.Order;
import payment_system.assignment.domain.Payment;
import payment_system.assignment.domain.PaymentMethod;
import payment_system.assignment.policy.DiscountPolicy;
import payment_system.assignment.policy.DiscountPolicyFactory;
import payment_system.assignment.repository.PaymentRepository;

import java.time.LocalDateTime;

@Service
public class PaymentService {
    private final DiscountPolicyFactory discountPolicyFactory;
    private final PaymentRepository paymentRepository;

    public PaymentService(DiscountPolicyFactory discountPolicyFactory,
                          PaymentRepository paymentRepository) {
        this.discountPolicyFactory = discountPolicyFactory;
        this.paymentRepository = paymentRepository;
    }

    public Payment processPayment(Order order, PaymentMethod paymentMethod) {
        Member member = order.getMember();
        DiscountPolicy discountPolicy = discountPolicyFactory.getPolicy(member.getGrade());
        int discountAmount = discountPolicy.calculateDiscountAmount(member, order.getOriginalPrice());
        int finalAmount = order.getOriginalPrice() - discountAmount;
        Payment payment = new Payment(null, order, finalAmount, paymentMethod, LocalDateTime.now());
        return paymentRepository.save(payment);
    }
}
