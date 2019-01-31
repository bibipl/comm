package mk.comm.Service;

import mk.comm.Repository.RoleRepository;
import mk.comm.Repository.UserRepository;
import mk.comm.Role.Role;
import mk.comm.User.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           BCryptPasswordEncoder bCryptPasswordEncoder)
    {
            this.passwordEncoder = bCryptPasswordEncoder;
            this.userRepository = userRepository;
            this.roleRepository = roleRepository;
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void saveUser(User user) {
        if (user.getId() == null || user.getPassword().equals(user.getPasswordCheck())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            if (user.getRoles() == null) {
                Role userRole = roleRepository.findByName("ROLE_USER");
                user.setRoles(new HashSet<Role>(Collections.singletonList(userRole)));
            }
        }
        userRepository.save(user);
    }

    @Override
    public void delete(User user) {
        userRepository.delete(user);
    }
}
