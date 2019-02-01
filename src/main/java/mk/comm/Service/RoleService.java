package mk.comm.Service;

import mk.comm.Role.Role;

import java.util.List;

public interface RoleService {
        Role findByName (String name);
        List<Role> findAll ();
        void save (Role role);
    }

