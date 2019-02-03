package mk.comm.Controller;

import mk.comm.Community.Community;
import mk.comm.Member.Member;
import mk.comm.Role.Role;
import mk.comm.Service.CommunityService;
import mk.comm.Service.MemberService;
import mk.comm.Service.UserService;
import mk.comm.User.CurrentUser;
import mk.comm.User.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    UserService userService;
    @Autowired
    CommunityService communityService;
    @Autowired
    MemberService memberService;

    //************* basic for admin after loging ***************//
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

    //************* MENU Community ***************//
    //***** Read community id of admin and load all members of the community ***** //
    @GetMapping("/community")
    public  String adminComm (@AuthenticationPrincipal CurrentUser currentUser, Model model) {
        User user = currentUser.getUser();
        List<Community> communities;
        if (user != null) {
            communities = communityService.findAllByUserId(user.getId());
            if (communities != null) {
                model.addAttribute("communities", communities);
                model.addAttribute("iam",user);
                return "/admin/showCommunities";
            }
        }
        return "redirect:/admin/";
    }

    @GetMapping("/community/add")
    public String communityAddForm (){
        return "";
    }

}
