package mk.comm.Service;

import mk.comm.Community.Community;
import mk.comm.Member.Member;

import java.util.List;

public interface MemberService {
    List<Member> findAllByCommunity (Community community);
}
