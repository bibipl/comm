package mk.comm.Service;

import mk.comm.Community.Community;
import mk.comm.Member.Member;

import java.util.List;

public interface MemberService {
    void save (Member member);
    void  delete (Member member);
    Member findById (Long id);
    List<Member> findAllByCommunityId(Long id);
    List<Member> findAllBySex (char sex);
    List<Member> findAllNotMarriedBySex (char sex);
    List<Member> findAllByCommunityIdOrderBySurname (Long id);
    List<Member> findAllByCommunityIdOrderBySurnameAscNameAsc (Long id);
    long countAllByCommunityId (Long idComm);


}
