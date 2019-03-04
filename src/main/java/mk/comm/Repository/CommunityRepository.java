package mk.comm.Repository;

import mk.comm.Community.Community;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityRepository extends JpaRepository<Community, Integer> {
    Community findById (Long id);
    List<Community> findAllByUserId (Long id);
    long countAllByUserId (long id);
}
