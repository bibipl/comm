package mk.comm.Service;

import mk.comm.User.User;

import java.util.List;

public interface UserService {
    User findByUsername(String username);
    User findById (Long id);
    List<User> findAll ();
    void saveUser(User user);
    void  delete (User user);
}
