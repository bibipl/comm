package mk.comm.Service;

import mk.comm.Circle.Circle;
import mk.comm.Group.Group;
import mk.comm.Repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
public class GroupServiceImpl implements GroupService {

    @Autowired
    GroupRepository groupRepository;
    @Autowired
    CircleService circleService;

    @Override
    public void save(Group group) {
        groupRepository.save(group);
    }

    @Override
    public void delete(Group group) {
// Delete Cascade - delete all dependent cicrcles if group is to be deleted.
        List<Circle> circles = circleService.findAllByGroupIdOrderByNumberAsc(group.getId());
        if (circles != null) {
            for (Circle circle : circles) {
                circleService.delete(circle);
            }
        }
        groupRepository.delete(group);
    }

    @Override
    public Group findById(Long groupId) {
        return groupRepository.findById(groupId);
    }

    @Override
    public List<Group> findAllByIdCommunity(Long idCommunity) {
        return groupRepository.findAllByIdCommunity(idCommunity);
    }
}
