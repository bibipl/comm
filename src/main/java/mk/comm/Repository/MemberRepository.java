package mk.comm.Repository;

import mk.comm.Community.Community;
import mk.comm.Member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Integer> {
List<Member> findAllByCommunityId(Long id);
}
