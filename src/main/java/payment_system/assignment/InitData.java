package payment_system.assignment;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import payment_system.assignment.domain.Member;
import payment_system.assignment.domain.MemberGrade;
import payment_system.assignment.domain.Order;
import payment_system.assignment.domain.PaymentMethod;
import payment_system.assignment.repository.MemberRepository;
import payment_system.assignment.repository.OrderRepository;
import payment_system.assignment.service.PaymentService;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class InitData implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    @Override
    public void run(String... args) {
        Member normalMember = memberRepository.save(new Member("일반회원", MemberGrade.NORMAL));
        Member vipMember = memberRepository.save(new Member("VIP회원", MemberGrade.VIP));
        Member vvipMember = memberRepository.save(new Member("VVIP회원", MemberGrade.VVIP));

        Order normalOrder = orderRepository.save(new Order("상품A", 10_000, normalMember));
        Order vipOrder = orderRepository.save(new Order("상품B", 10_000, vipMember));
        Order vvipOrder = orderRepository.save(new Order("상품C", 10_000, vvipMember));

        paymentService.processPayment(normalOrder.getId(), PaymentMethod.CREDIT_CARD);
        paymentService.processPayment(vipOrder.getId(), PaymentMethod.CREDIT_CARD);
        paymentService.processPayment(vvipOrder.getId(), PaymentMethod.POINT);
    }
}
