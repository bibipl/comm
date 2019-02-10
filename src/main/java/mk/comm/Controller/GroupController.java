package mk.comm.Controller;

import mk.comm.Community.Community;
import mk.comm.Group.Group;
import mk.comm.Service.*;
import mk.comm.User.CurrentUser;
import mk.comm.User.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/groups")
public class GroupController {

    @Autowired
    GroupService groupService;
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

    //************* MENU Groups ***************//
    //***** Read community id,s that have admin_id = admin.getId() of admin of admin  ***** //
    @GetMapping("")
    public  String showAllGroups (@AuthenticationPrincipal CurrentUser currentUser, Model model) {
        User user = currentUser.getUser();
        if (user != null && user.getId() > 0) {
            List<Community> communities = communityService.findAllByUserId(user.getId());
            List<Group> allGroups = new ArrayList<>();
            if (communities != null) {
                for (Community community : communities) {
                    if (community.getId() >0 ) {
                        allGroups.addAll(groupService.findAllByIdCommunity(community.getId()));
                    }
                }
                model.addAttribute("communities", communities);
                model.addAttribute("allGroups", allGroups);
                model.addAttribute("iam", user);
                return "/groups/showGroups";
            }
        }
        return "redirect:/admin/";
    }

    // *** This one shows all members of the community od specified id *****    //
    // ** for safety reason comparing admin's id wiht community user_id ***** //
    @GetMapping("/view/{id}")
    public String communityView (@AuthenticationPrincipal CurrentUser currentUser,
                                 @PathVariable Long id,
                                 Model model){
        User user = currentUser.getUser();
        if (user != null && user.getId() > 0 && id > 0) {
            Group group = groupService.findById(id);
        ///// ***** further code....
        }
        return "redirect:/admin/community";
    }

    //***** Here we add new group ****//
    @GetMapping("/add/{idComm}")
    public String groupAddForm (@AuthenticationPrincipal CurrentUser currentUser,
                                @PathVariable Long idComm, Model model){
        User user = currentUser.getUser();
        Community selCommunity = communityService.findById(idComm);
        Group group = new Group();
        if (user != null && user.getId() > 0 && selCommunity != null) {
            List<Community> communities = communityService.findAllByUserId(user.getId());
            group.setIdCommunity(idComm);
            model.addAttribute("communities", communities);
            model.addAttribute("selCommunity", selCommunity);
            model.addAttribute("group", group);
            model.addAttribute("iam", user);
            return "/groups/addGroup";
        }
        return "redirect:/admin/groups";
    }
    //***** continue to add new community (just name and admin's id *****//
    @PostMapping("/add")
    public String groupAddSave (@AuthenticationPrincipal CurrentUser currentUser,
                                @ModelAttribute Group group, BindingResult result) {
        if (result.hasErrors()) {
            return ("redirect:/admin/groups/add");
        }
        String nameError = "Nazwa grupy nie może być pusta !!!";
        if (group.getName() == null || group.getName().equals("") || group.getName().equals(nameError)) {
            group.setName(nameError);
            return "/admin/addGroup";
        }
        User user = currentUser.getUser();
        if (user != null && user.getId() > 0 && group != null && group.getIdCommunity() > 0) {
            Community community = communityService.findById(group.getIdCommunity());
            if (community != null && community.getUserId() != null && community.getUserId() == user.getId()) {
                groupService.save(group);
            }
        }
        return ("redirect:/admin/groups");
    }
}
