package mk.comm.Repository;

import mk.comm.User.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findById (Long id);
    User findByUsername(String username);
}
