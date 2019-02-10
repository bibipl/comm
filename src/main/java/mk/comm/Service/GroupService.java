package mk.comm.Service;

import mk.comm.Group.Group;

import java.util.List;


public interface GroupService {
    void save (Group group);
    void  delete (Group group);
    Group findById (Long groupId);
    List<Group> findAllByIdCommunity (Long idCommunity);

}
