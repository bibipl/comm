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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.jws.soap.SOAPBinding;
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
    //***** Read community id,s that have admin_id = admin.getId() of admin of admin  ***** //
    @GetMapping("/communities")
    public  String adminComm (@AuthenticationPrincipal CurrentUser currentUser, Model model) {
        User user = currentUser.getUser();
        List<Community> communities;
        if (user != null && user.getId() > 0) {
            communities = communityService.findAllByUserId(user.getId());
            if (communities != null) {
                model.addAttribute("communities", communities);
                model.addAttribute("iam",user);
                return "/admin/showCommunities";
            }
        }
        return "redirect:/admin/";
    }

    // *** This one shows all members of the community od specified id *****    //
    // ** for safety reason comparing admin's id wiht community user_id ***** //
    @GetMapping("/community/view/{id}")
    public String communityView (@AuthenticationPrincipal CurrentUser currentUser,
                                 @PathVariable Long id,
                                 Model model){
        User user = currentUser.getUser();
        Community community;
        List <Member> members;
        if (user != null && user.getId() > 0 && id > 0) {
            community = communityService.findById(id);
            if (community != null && community.getId() != 0 && community.getUserId() == user.getId()) {
                members = memberService.findAllByCommunityId(community.getId());
                if (members != null) {
                    model.addAttribute("community", community);
                    model.addAttribute("members", members);
                    model.addAttribute("iam", user);
                    return "/admin/showMembers";
                }
            }
        }
        return "redirect:/admin/communities";
    }

    //***** Here we add new community for an admin ****//
    @GetMapping("/community/add")
    public String communityAddForm (@AuthenticationPrincipal CurrentUser currentUser, Model model){
        User user = currentUser.getUser();
        Community community = new Community();
        if (user != null && user.getId() > 0) {
           model.addAttribute("community", community);
           model.addAttribute("iam", user);
           return "/admin/addCommunity";
        }
        return "redirect:/admin/communities";
    }
    //***** continue to add new community (just name and admin's id *****//
    @PostMapping("community/add")
    public String communityAddSave (@AuthenticationPrincipal CurrentUser currentUser, @ModelAttribute Community community, BindingResult result) {
        if (result.hasErrors()) {
            return ("redirect:/admin/community/add");
        }
        String nameError = "Nazwa wspólnoty nie może być pusta !!!";
        if (community.getName() == null || community.getName().equals("") || community.getName().equals(nameError)) {
            community.setName(nameError);
            return "/admin/addCommunity";
        }
        User user = currentUser.getUser();
        if (user != null && user.getId() > 0) {
            community.setUserId(user.getId());
        }
        communityService.save(community);
        return ("redirect:/admin/communities");
    }



    @GetMapping("/community/edit/{id}")
    public String communityEditForm (@AuthenticationPrincipal CurrentUser currentUser,
                                     @PathVariable Long id, Model model){
        User user = currentUser.getUser();
        Community community;
        if (user != null && user.getId() > 0) {
            if(id > 0) {
                community = communityService.findById(id);
                if(community != null && community.getId() > 0 && community.getUserId() == user.getId()) {
                    model.addAttribute("community", community);
                    model.addAttribute("iam", user);
                    return "/admin/addCommunity";
                }
            }
        }
        return "redirect:/admin/communities";

    }
    //***** here we deltete community - we will display community and ask for conformation ****** ///
    @GetMapping("/community/delete/{id}")
    public String communityDeleteForm (@AuthenticationPrincipal CurrentUser currentUser,
                                       @PathVariable Long id, Model model){
        User user = currentUser.getUser();
        Community community;
        if (user != null && user.getId() > 0) {
            if(id > 0) {
                community = communityService.findById(id);
                if(community != null && community.getId() > 0 && community.getUserId() == user.getId()) {
                    List<Member> members = memberService.findAllByCommunityId(id);
                    int ilenas = members.size();
                    model.addAttribute("ilenas", ilenas);
                    model.addAttribute("community", community);
                    model.addAttribute("iam", user);
                    return "/admin/deleteCommunity";
                }
            }
        }
        return "redirect:/admin/communities";
    }

    // *** here by 'submit' we cave confirmation to delete community ****** //
    @PostMapping("community/delete")
    public String communityDeleteSave (@AuthenticationPrincipal CurrentUser currentUser,
                                       @ModelAttribute Community community) {

        User user = currentUser.getUser();
        if (user != null && user.getId() > 0) {
           if (community != null && community.getId() >0 && community.getUserId() == user.getId()) {
               Long id = community.getId();
               community = communityService.findById(id);
               if (community != null) {
                   communityService.delete(community);
               }
           }
        }
        return ("redirect:/admin/communities");
    }

}
