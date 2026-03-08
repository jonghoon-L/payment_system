package payment_system.assignment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import payment_system.assignment.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
