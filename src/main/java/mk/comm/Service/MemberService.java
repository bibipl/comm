package mk.comm.Service;

import mk.comm.Community.Community;
import mk.comm.Member.Member;

import java.util.List;

public interface MemberService {
    void save (Member member);
    List<Member> findAllByCommunityId(Long id);

}
