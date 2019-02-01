package mk.comm.Service;

import mk.comm.Community.Community;
import mk.comm.Member.Member;
import mk.comm.Repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MemberServiceImpl implements MemberService{

    @Autowired
    MemberRepository memberRepository;

    @Override
    public List<Member> findAllByCommunity(Community community) {
        return memberRepository.findAllByCommunity(community);
    }
}
