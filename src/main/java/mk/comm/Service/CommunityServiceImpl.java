package mk.comm.Service;

import mk.comm.Community.Community;
import mk.comm.Repository.CommunityRepository;
import mk.comm.User.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CommunityServiceImpl implements CommunityService{

    @Autowired
    CommunityRepository communityRepository;

    @Override
    public void save(Community community) {
        communityRepository.save(community);
    }

    @Override
    public void delete(Community community) {
        communityRepository.delete(community);
    }

    @Override
    public Community findById(Long id) {
        return communityRepository.findById(id);
    }

    @Override
    public List<Community> findAllByUserId(Long id) {
        return communityRepository.findAllByUserId(id);
    }


}
