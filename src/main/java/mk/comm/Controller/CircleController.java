package mk.comm.Controller;

import com.sun.org.apache.xpath.internal.operations.Mod;
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
    @GetMapping("/addCircle/{idGroup}")
    public String addCircle (@AuthenticationPrincipal CurrentUser currentUser,
                            @PathVariable Long idGroup, Model model) {
        User user = currentUser.getUser();
          if (checkAdminCreentialGroup(user,idGroup)) {
              Group group = groupService.findById(idGroup);
              List<Member> membersCircleFree = membersNotInTheCircleYet(group);
              Circle circle = new Circle();
              model.addAttribute("idComm", group.getIdCommunity());
              model.addAttribute("idGro", idGroup);
              model.addAttribute("circle", circle);
              model.addAttribute("members", membersCircleFree);
              model.addAttribute("iam", user);
              return "/circles/addCircle";
          }
        if (idGroup >0 )
            return "redirect:/admin/groups/view/" + idGroup;
        return "redirect:admin/groups";
    }
    @PostMapping ("/addCircle/{idGroup}")
    public String addCircleAction (@AuthenticationPrincipal CurrentUser currentUser,
                                   @PathVariable Long idGroup,
                                   @RequestParam(value = "circleMembers", required = false) long[] circleMembers,
                                   @ModelAttribute Circle circle, BindingResult result) {
        User user = currentUser.getUser();
        if (circle.getId() > 0) {
            circle = circleService.findById(circle.getId());
        }
        addCircleAction(user, idGroup, circleMembers, circle);
        if (idGroup >0 )
            return "redirect:/admin/groups/view/" + idGroup;
        return "redirect:admin/groups";
    }

    //Edit number of the circle : to be improved so for just to be workable
    @GetMapping("/editCircle/{idCircle}")
    public String editCircle (@AuthenticationPrincipal CurrentUser currentUser,
                              @PathVariable Long idCircle, Model model) {
        User user = currentUser.getUser();
        if (checkAminCredentialCircle(user, idCircle)) {
            Circle circle = circleService.findById(idCircle);
            model.addAttribute("circle", circle);
            return "/circles/editCircle";
        }
        return"/";
    }
    @PostMapping ("/editCircle")
    public String editCircleAction (@AuthenticationPrincipal CurrentUser currentUser,
                                   @ModelAttribute Circle circle, BindingResult result) {
        User user = currentUser.getUser();
        if (circle != null) {
            int num = circle.getNumber();
            if (num > 0) {
                circle = circleService.findById(circle.getId());
                circle.setNumber(num);
                circleService.save(circle);
            }
            if (circle.getGroupId() > 0)
                return "redirect:/admin/groups/view/" + circle.getGroupId();
            return "redirect:admin/groups";
        }
        return "redirect:/";
    }

    // delete specified circlecircle
    @GetMapping("/deleteCircle/{idCircle}")
    public String deleteCircle (@AuthenticationPrincipal CurrentUser currentUser,
                                @PathVariable Long idCircle, Model model) {
        User user = currentUser.getUser();
        if (checkAminCredentialCircle(user, idCircle)) {
            Circle circle = circleService.findById(idCircle);
            model.addAttribute("circle", circle);
            return "/circles/deleteCircle";
        }
        return"/";
    }
    @PostMapping ("/deleteCircle")
    public String deleteCircleAction (@AuthenticationPrincipal CurrentUser currentUser,
                                      @ModelAttribute Circle circle) {
        // we pass here only circle id in the circle object.
        User user = currentUser.getUser();
        if (circle != null) {
            circle = circleService.findById(circle.getId());
            Long idGroup = circle.getGroupId();
            if (idGroup > 0) {
                if (checkAdminCreentialGroup(user, circle.getGroupId())) {
                    circleService.delete(circle);
                    return "redirect:/admin/groups/view/" + idGroup;
                }
            }
        }
        return "redirect:admin/groups";
    }

    // change responsible. To be taken into account - free prev, add new, assumtion
    // Resp can be (in case of Marriages) - only husband, adn wife we add by default.
    @GetMapping("/changeResp/{idCircle}/{idMember}")
    public String changeResp (@AuthenticationPrincipal CurrentUser currentUser,
                              @PathVariable Long idCircle, @PathVariable Long idMember,
                              Model model) {

        User user = currentUser.getUser();
        if (checkAminCredentialCircle (user, idCircle)) {
            Circle circle = circleService.findById(idCircle);
            if (circle != null) {
                circle = changeRespAction(circle, idMember);
            }
            if (circle != null) {
                circleService.save(circle);
            }
            return "redirect:/admin/groups/view/" + circle.getGroupId();
        }
        return "redirect:/";
    }

    // adds a nie memeber to the circle (idCircle) - you can choose form not assigned to the circle members od the community
    @GetMapping("/addMemb/{idCircle}")
    public String addMemb (@AuthenticationPrincipal CurrentUser currentUser,
                           @PathVariable Long idCircle, Model model) {
        User user = currentUser.getUser();
        if (checkAminCredentialCircle(user,idCircle)) {
            Circle circle = circleService.findById(idCircle);
            if (circle.getGroupId() > 0) {
                Group group = groupService.findById(circle.getGroupId());
                List<Member> membersCircleFree = membersNotInTheCircleYet(group);
                model.addAttribute("idComm", group.getIdCommunity());
                model.addAttribute("idGro", circle.getGroupId());
                model.addAttribute("circle", circle);
                model.addAttribute("members", membersCircleFree);
                model.addAttribute("iam", user);
                return "/circles/addCircle";
            }
        }
        return "/";
    }

    // remove a member (idMemb) from the circle (idCircle)
    @GetMapping ("/removeMemb/{idCircle}/{idMember}")
    public String removeMembFromCircle (@AuthenticationPrincipal CurrentUser currentUser,
                                        @PathVariable Long idCircle, @PathVariable Long idMember) {
        User user = currentUser.getUser();
        Member member = new Member();
        if (idMember > 0) {
            member = memberService.findById(idMember);
        }
        if (idCircle >0 && checkAminCredentialCircle(user, idCircle)) {
            Circle circle = circleService.findById(idCircle);
            boolean newResp = false;
            if (circle.getMembers() != null && circle.getMembers().contains(member)) {
                if (circle.getResponsible() == member.getId()) {
                    if (circle.getMembers().size() > 1) {
                        newResp = true;
                    } else {
                        circle.setResponsible((long)0);
                    }
                }
                circle.getMembers().remove(member);
                if (newResp && circle.getMembers().size() > 0) {
                    circle = changeRespAction( circle, circle.getMembers().get(0).getId());
                }
                circleService.save(circle);
            }
            return "redirect:/admin/groups/view/" + circle.getGroupId();
        }

        return "redirect:/";
    }


// *********************************************************
// ******* Working methods for more complicated works ******
// *********************************************************

    // *** checks if user has right to change anything in the "circle".
    // *** additionally confirms that community, group and circle are not empty and have not empty id to parent.
        private boolean checkAminCredentialCircle (User user, Long idCircle) {
        boolean credential = false;
        if (idCircle > 0) {
            Circle circle = circleService.findById(idCircle);
            if (circle != null && circle.getGroupId() > 0) {
                credential = checkAdminCreentialGroup(user, circle.getGroupId());
            }
        }
        return credential;
    }

    // *** checks if user has right to change anything in the "group".
    // *** additionally confirms that community and group are not empty and have not empty id to parent.
    private  boolean checkAdminCreentialGroup (User user, Long idGroup) {
        boolean credential = false;
        if (idGroup > 0) {
            Group group = groupService.findById(idGroup);
            if (group != null && group.getIdCommunity() > 0) {
                credential = checkAdminCreentialCommunity(user, group.getIdCommunity());
            }
        }
        return credential;
    }
    // *** checks if user has right to change anything in the "community".
    // *** additionally confirms that community ais not empty and have not empty id to parent.
    private  boolean checkAdminCreentialCommunity (User user, Long idCommunity) {
        boolean credential = false;
        if (idCommunity > 0) {
            Community community = communityService.findById(idCommunity);
            if (community != null && user != null && user.getId() >0) {
                credential = true;
            }
        }
        return credential;
    }

    // with table of members' ids and id of the group (circle belongs to) and circle partly filled (in the form) creeates cicrcle with members
    // it check user if has credential to make a change.
    private void addCircleAction(User user, Long idGroup, long[] circleMembers, Circle circle) {
        if ( checkAdminCreentialGroup(user, idGroup) && circleMembers != null) {
            Group group = groupService.findById(idGroup); // Może id group też Request Param przekazać ?
            int numOfCircles = circleService.countAllByGroupId(group.getId());
            if (circle.getNumber() == 0) {
                circle.setNumber(numOfCircles + 1);
            }
            for (long idMemb : circleMembers) {
                Member member = memberService.findById(idMemb);
                circle.getMembers().add(member);
            }
            if (circle.getResponsible() == 0 && circle.getMembers() != null) {
                circle.setResponsible(circle.getMembers().get(0).getId()); /// here to be taken by checkbox - save circle and go to choose responsible, with checked (0)
            }
            if (circle.getGroupId() == 0) {
                circle.setGroupId(group.getId());
            }
            if (circle.getId() == null) {
                circleService.save(circle); // no id's save not properly sorted we have to save 2x
            }
            circle=Circle.SortByName(circle);
            circleService.save(circle);
        }
    }


    // ****With input group, returns all members from group that have not been assignet to any circle in the group yet.
    private List<Member> membersNotInTheCircleYet(Group group) {

        //1. All group's members
        List<Member> members = memberService.findAllByCommunityId(group.getIdCommunity());
        // 2. Iterate all Circles form the group and add to List members in Circle
        List<Circle> circlesExisting = circleService.findAllByGroupIdOrderByNumberAsc(group.getId());
        List<Member> membersCircleBusy = new ArrayList<>();

        for (Circle circle : circlesExisting) {
            for (Member member : circle.getMembers()) {
                membersCircleBusy.add(member);
            }
        }
        // take ony ones that do not belong to any circle of the group :
        List<Member> membersCircleFree = new ArrayList<>();
        for (Member member : members) {
            if (!membersCircleBusy.contains(member)) {
                membersCircleFree.add(member);
            }

        }
        return membersCircleFree;
    }


    // changes actual Responsible in the circle "idCircle" for a noew one "idMember"
    private Circle changeRespAction(Circle circle, Long idMember) {
        if (idMember > 0) {
            Member memberRespNew = memberService.findById(idMember);
            if (memberRespNew != null) {
                if (memberRespNew.getMarried() >0 && memberRespNew.getSex() == 'K') {
                    for (Member member : circle.getMembers()) {
                        if (member.getMarried() == memberRespNew.getId()) {
                        // if there is a husband in the circle.
                            idMember = memberRespNew.getMarried();
                        }
                    }
                }
                circle.setResponsible(idMember);
                return circle;
            }
        }
        return null;
    }
}
