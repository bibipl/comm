package mk.comm.Controller;

import mk.comm.Circle.Circle;
import mk.comm.Community.Community;
import mk.comm.Group.Group;
import mk.comm.Member.Member;
import mk.comm.Service.*;
import mk.comm.User.CurrentUser;
import mk.comm.User.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/circles")
public class CircleController {

    @Autowired
    MemberService memberService;
    @Autowired
    CircleService circleService;
    @Autowired
    GroupService groupService;
    @Autowired
    CommunityService communityService;
    @Autowired
    UserService userService;


    // Add a new Circle to the specified Group (idGroup)
    @GetMapping("/add/{idGroup}")
    public String addCircle (@AuthenticationPrincipal CurrentUser currentUser,
                            @PathVariable Long idGroup, Model model) {
        User user = currentUser.getUser();
        if (user != null && idGroup > 0) { // we know there is and authentic user.
            Group group = groupService.findById(idGroup);
            if (group != null && group.getIdCommunity() > 0) { // we know group is not empte and belongs to existing community
                Community community = communityService.findById(group.getIdCommunity());
                if (community != null && community.getUserId() == user.getId()) { // we know community not null and use has rights
                   // now we can start circle creation :
                    // 1. get all members.
                    List<Member> members = memberService.findAllByCommunityId(community.getId());
                    // 2. Iterate all Circles form the group and add to List members in Circle
                    List<Circle> circlesExisting= circleService.findAllByGroupIdOrderByNumberAsc(group.getId());
                    List<Member> membersCircleBusy = new ArrayList<>();

                    for (Circle circle : circlesExisting) {
                        for (Member member : circle.getMembers()) {
                            membersCircleBusy.add(member);
                        }
                    }
                    // take ony ones that do not belong to any circle of the group :
                    List <Member> membersCircleFree = new ArrayList<>();
                    for (Member member : members) {
                        if (!membersCircleBusy.contains(member)) {
                            membersCircleFree.add(member);
                        }

                    }
                    Circle circle = new Circle();
                    model.addAttribute("idComm", community.getId());
                    model.addAttribute("idGro", idGroup);
                    model.addAttribute("circle", circle);
                    model.addAttribute("members", membersCircleFree);
                    model.addAttribute("iam", user);
                    return "/circles/addCircle";
                }
            }
        }
        if (idGroup >0 )
            return "redirect:/admin/groups/view/" + idGroup;
        return "redirect:admin/groups";
    }
    @PostMapping ("/add/{idGroup}")
    public String addCircleAction (@AuthenticationPrincipal CurrentUser currentUser,
                                   @PathVariable Long idGroup,
                                   @RequestParam(value = "circleMembers", required = false) long[] circleMembers,
                                   @ModelAttribute Circle circle, BindingResult result) {

        // TODO check user credentials;
        if (circleMembers != null) {
            Group group = groupService.findById(idGroup); // Może id group też Request Param przekazać ?
            int numOfCircles = circleService.countAllByGroupId(group.getId());
            List<Member> members = new ArrayList<>();
            circle.setNumber(numOfCircles + 1);
            for (long idMemb : circleMembers) {
                Member member = memberService.findById(idMemb);
                members.add(member);
            }
            circle.setResponsible(members.get(0).getId()); /// here to be taken by checkbox - save circle and go to choose responsible, with checked (0)
            circle.setMembers(members);
            circle.setGroupId(group.getId());
            circleService.save(circle);
        }
        if (idGroup >0 )
            return "redirect:/admin/groups/view/" + idGroup;
        return "redirect:admin/groups";
    }
}
