package payment_system.assignment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_discount_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentDiscountHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(nullable = false)
    private String memberGrade;

    @Column(nullable = false)
    private String policyName;

    @Column(nullable = false)
    private int discountRate;

    @Column(nullable = false)
    private int discountAmount;

    public PaymentDiscountHistory(Payment payment, String memberGrade, String policyName, int discountRate, int discountAmount) {
        this.payment = payment;
        this.memberGrade = memberGrade;
        this.policyName = policyName;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
    }
}
