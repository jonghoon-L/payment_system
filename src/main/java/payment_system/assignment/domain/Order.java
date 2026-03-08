package payment_system.assignment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private int originalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public Order(String itemName, int originalPrice, Member member) {
        this.itemName = itemName;
        this.originalPrice = originalPrice;
        this.member = member;
    }

    public Order(Long id, String itemName, int originalPrice, Member member) {
        this.id = id;
        this.itemName = itemName;
        this.originalPrice = originalPrice;
        this.member = member;
    }
}
