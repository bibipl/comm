package mk.comm.Controller;

import mk.comm.Role.Role;
import mk.comm.Service.RoleService;
import mk.comm.Service.UserService;
import mk.comm.User.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Set;

@Controller
public class RegisterController {
    @Autowired
    UserService userService;
    @Autowired
    RoleService roleService;

    @GetMapping("/register")
    public String registerForm (Model model) {
        User user = new User();
        model.addAttribute("user", user)  ;
        return "register";
    }
    @PostMapping("/register")
    public String registerAction (@ModelAttribute @Valid User user, BindingResult result) {
        if (result.hasErrors()) {
            return "/user/register";
        }
        else if (!user.getPassword().equals(user.getPasswordCheck())) {
            return"/user/register";
        }

        User userCheckUsername = userService.findByUsername(user.getUsername());

        if (userCheckUsername == null) {
            Role role=roleService.findByName("ROLE_USER");
            Set<Role> allRoles = new HashSet<>();
            allRoles.add(role);
            user.setRoles(allRoles);
            user.setEnabled(0);
            userService.saveUser(user); // we have to save to get id.
            return "user/login";
        }
        return "/user/register";
    }
}
