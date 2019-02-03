package mk.comm.Service;

import mk.comm.Community.Community;
import mk.comm.User.User;

import java.util.List;

public interface CommunityService {
    void save (Community community);
    Community findByUserId (Long id);
    List<Community> findAllByUserId (Long id);


}
