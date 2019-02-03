package mk.comm.Service;

import mk.comm.Community.Community;
import mk.comm.User.User;

import java.util.List;

public interface CommunityService {
    void save (Community community);
    void delete (Community community);
    Community findById (Long id);
    List<Community> findAllByUserId (Long id);


}
