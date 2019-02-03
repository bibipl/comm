package mk.comm.Controller;

import mk.comm.Role.Role;
import mk.comm.User.CurrentUser;
import mk.comm.User.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping ("/owner")
public class OwnerController {

    @GetMapping("/")
        public String ownerStart(@AuthenticationPrincipal CurrentUser currentUser, Model model) {
            User user = currentUser.getUser();
            for (Role role : user.getRoles()) {
                if (role.getName().equals("ROLE_OWNER")) {
                    model.addAttribute("iam", user);
                    return "/owner/start";
                }
            }
            return "landing";
        }

}
