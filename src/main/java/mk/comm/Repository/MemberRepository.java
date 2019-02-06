package mk.comm.Repository;

import mk.comm.Community.Community;
import mk.comm.Member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Integer> {
    Member findById (Long id);
    List<Member> findAllByCommunityId(Long id);
    List<Member> findAllBySex (char sex);
    @Query ("select m from Member m where m.sex=?1 and m.married=0")
    List<Member> findAllMarriedBySex (char sex);
}
