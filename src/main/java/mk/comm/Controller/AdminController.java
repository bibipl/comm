package mk.comm.Controller;

import mk.comm.Role.Role;
import mk.comm.Service.CommunityService;
import mk.comm.Service.EmailSender;
import mk.comm.Service.MemberService;
import mk.comm.Service.UserService;
import mk.comm.User.CurrentUser;
import mk.comm.User.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    UserService userService;
    @Autowired
    CommunityService communityService;
    @Autowired
    MemberService memberService;
    @Autowired
    EmailSender emailSender;
    @Autowired
    TemplateEngine templateEngine;

    //************* basic for admin after loging ***********************//
    //*** if you are here means you have your own community to rule ****//
    //*** so show your resources ***************************************//
    //*** **************************************************************//
    @GetMapping("/")
    public String adminStart(@AuthenticationPrincipal CurrentUser currentUser, Model model) {
        User user = currentUser.getUser();
        for (Role role : user.getRoles()) {
            if (role.getName().equals("ROLE_ADMIN")) {
                model.addAttribute("iam", user);
                return "/admin/start";
            }
        }
        return "landing";
    }
    //************************************************//
    //***  here admin wants to update admin's data  ***//
    //************************************************//
    @GetMapping("/edit")
    public  String editSelfAdmin () {
        return"";
    }

    @PostMapping("/edit")
    public  String editSelfAdminAction () {
        return"";
    }
    //******************************************************************//
    //***  here admin wants to delete admin's data  *******************//
    //****we want to be sure all the related data are also deleted ***//
    //***************************************************************//

    @GetMapping("/delete")
    public  String deleteSelfAdmin () {
        return"";
    }
    @PostMapping("/delete")
    public  String deleteSelfAdminAction () {
        return"";
    }













}
