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
    public void save(Member member) {
        memberRepository.save(member);

    }

    @Override
    public List<Member> findAllByCommunityId(Long id) {
        return memberRepository.findAllByCommunityId(id);
    }
}
