package mk.comm;

import mk.comm.Role.Role;
import mk.comm.Service.RoleService;
import mk.comm.Service.UserService;
import mk.comm.User.CurrentUser;
import mk.comm.User.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Start {

    @Autowired
    UserService userService;
    @Autowired
    RoleService roleService;

    @GetMapping("/")
    public String start(@AuthenticationPrincipal CurrentUser customUser, Model model) {

        // START of usual code
        if (customUser != null) {
            User entityUser = customUser.getUser();
            model.addAttribute("currentUser", entityUser);
            boolean isAdmin = false;
            boolean isUser = false;
            boolean isOwner = false;
            for (Role role : entityUser.getRoles()) {
                if (role.getName().equals("ROLE_OWNER")) {
                    isOwner = true;
                }
                if (role.getName().equals("ROLE_ADMIN")) {
                    isAdmin = true;
                }
                if (role.getName().equals("ROLE_USER")) {
                    isUser = true;
                }

            }
            if (isOwner) return "redirect:/owner/"; // owner can admin admins & all users
            if (isAdmin) return "redirect:/admin/"; // admin - admin own users in own community
            if (isUser) return "redirect:/user/";   //  user - belongs to community
        }
        // END of usual code
        // START od firts time code
        /*Role role0 = new Role();
        Role role1 = new Role();
        Role role2 = new Role();
        role0.setName("ROLE_OWNER");
        role1.setName("ROLE_ADMIN");
        role2.setName("ROLE_USER");
        roleService.save(role0);
        roleService.save(role1);
        roleService.save(role2);
        Set<Role> roles = new HashSet<>();
        roles.add(role0);
        roles.add(role1);
        roles.add(role2);
        User user = new User();
        user.setUsername("email@email.com"); <- here your email that will be also a login
        user.setName("Name");
        user.setSurname("Surname);
        user.setPassword("123");
        user.setPasswordCheck("123");
        user.setRoles(roles);
        userService.saveUser(user);*/
        // END od first time code
        return "landing";
    }
}


