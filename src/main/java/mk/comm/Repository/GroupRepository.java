package mk.comm.Repository;

import mk.comm.Group.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Integer> {
    Group findById (Long groupId);
    List<Group> findAllByIdCommunity (Long idCommunity);
}
