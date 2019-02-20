package mk.comm.Controller;

import mk.comm.Circle.Circle;
import mk.comm.Community.Community;
import mk.comm.Event.Event;
import mk.comm.Group.Group;
import mk.comm.Member.Member;
import mk.comm.Member.MemberAttr;
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

import java.util.*;

@Controller
@RequestMapping("/admin/groups")
public class GroupController {

    @Autowired
    EventService eventService;
    @Autowired
    CircleService circleService;
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

    @GetMapping("/{idComm}")
    public  String showAllGroupsByCommunity (@AuthenticationPrincipal CurrentUser currentUser,
                                             @PathVariable Long idComm, Model model) {
        User user = currentUser.getUser();
        if (user != null && user.getId() > 0) {
            if (idComm != null) {
                Community community = communityService.findById(idComm);
                List<Group> groups = new ArrayList<>();
                if (community != null) {
                    groups = groupService.findAllByIdCommunity(community.getId());
                    model.addAttribute("idComm", community.getId());
                    model.addAttribute("communities", community);
                    model.addAttribute("allGroups", groups);
                    model.addAttribute("iam", user);
                    return "/groups/showGroups";
                }
            }
        }
        return "redirect:/admin/";
    }

    // *** This one shows all members of the community od specified id *****    //
    // ** for safety reason comparing admin's id wiht community user_id ***** //
    @GetMapping("/view/{idGroup}")
    public String communityView (@AuthenticationPrincipal CurrentUser currentUser,
                                 @PathVariable Long idGroup,
                                 Model model){
        User user = currentUser.getUser();

        if (idGroup > 0 && checkAminCredential(user, idGroup)) {
            Group group = groupService.findById(idGroup);
            Community community = communityService.findById(group.getIdCommunity());
            List<Circle> circles = circleService.findAllByGroupIdOrderByNumberAsc(group.getId());
            List<Event> events = new ArrayList<>();
            if (circles != null) {
                for (Circle circle : circles) {
                   List<Event> tempListEvents = eventService.findAllByCircleIdOrderByDate(circle.getId());
                   events.addAll(tempListEvents);
                }
            }

            model.addAttribute("events", events);
            model.addAttribute("group", group);
            model.addAttribute("circles", circles);
            model.addAttribute("community", community);
            model.addAttribute("iam", user);
            return "/groups/showDetails";
        ///// ***** further code....
        }
        return "redirect:/admin/community";
    }

    //***** Here we add new group ****//
    @GetMapping("/add/{idComm}")
    public String groupAddForm (@AuthenticationPrincipal CurrentUser currentUser,
                                @PathVariable Long idComm, Model model){
        User user = currentUser.getUser();
        Group group = new Group();
        if (user != null && user.getId() > 0) {
            List<Community> communities = communityService.findAllByUserId(user.getId());
            if (communities != null) {
                if (idComm == 0) {
                    idComm = communities.get(0).getId();
                }
                Community selCommunity = communityService.findById(idComm);
                group.setIdCommunity(idComm);
                model.addAttribute("communities", communities);
                model.addAttribute("selCommunity", selCommunity);
                model.addAttribute("group", group);
                model.addAttribute("iam", user);
                return "/groups/addGroup";
            }
        }
        return "redirect:/admin/groups";
    }

    @GetMapping("/edit/{idGroup}")
    public String groupEditForm (@AuthenticationPrincipal CurrentUser currentUser,
                                @PathVariable Long idGroup, Model model){
        User user = currentUser.getUser();
        if ( user != null && user.getId() > 0 && idGroup > 0) {
            Group group = groupService.findById(idGroup);
            if (group.getIdCommunity() > 0) {
                Community selCommunity = communityService.findById(group.getIdCommunity());
                List<Community> communities = communityService.findAllByUserId(user.getId());
                if (selCommunity != null && communities != null) {
                    model.addAttribute("communities", communities);
                    model.addAttribute("selCommunity", selCommunity);
                    model.addAttribute("group", group);
                    model.addAttribute("iam", user);
                    return "/groups/addGroup";
                }
                return "redirect:/admin/groups/" + group.getIdCommunity();
            }
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
        return ("redirect:/admin/groups/" + group.getIdCommunity());
    }

    //***** here we deltete community - we will display community and ask for conformation ****** ///
    @GetMapping("/delete/{idGroup}")
    public String communityDeleteForm (@AuthenticationPrincipal CurrentUser currentUser,
                                       @PathVariable Long idGroup, Model model){
        User user = currentUser.getUser();
        Group group = null;
        if (user != null && user.getId() > 0) {
            if(idGroup > 0) {
                group = groupService.findById(idGroup);
                if(group != null && group.getId() > 0) {
                    Community community = communityService.findById(group.getIdCommunity());
                    if (community != null && community.getUserId() >0 && community.getUserId() == user.getId()) {
                        model.addAttribute("community", community);
                        model.addAttribute("group", group);
                        model.addAttribute("iam", user);
                        return "/groups/deleteGroup";
                    }
                    if (group.getIdCommunity() == 0) groupService.delete(group);
                    return "redirect:/admin/groups";
                }
            }
            return "redirect:/admin/groups/" + group.getIdCommunity();
        }
        return "redirect:/admin/groups";
    }

    // *** here by 'submit' we cave confirmation to delete community ****** //
    @PostMapping("/delete")
    public String communityDeleteSave (@AuthenticationPrincipal CurrentUser currentUser,
                                       @ModelAttribute Group group) {
        User user = currentUser.getUser();
        Community community = null;
        if (group.getIdCommunity() != 0) {
            community = communityService.findById(group.getIdCommunity());
            if (user != null && user.getId() > 0 && community != null && community.getUserId().equals(user.getId())) {
                    groupService.delete(group);
                }
            }
        if (community.getId() != 0) {
            return ("redirect:/admin/groups/" + community.getId());
        }
        return "redirect:/admin/groups";
    }

    @GetMapping("/random/{idComm}")
    public String randomCircleDivisionForm (@AuthenticationPrincipal CurrentUser currentUser,
                                            @PathVariable Long idComm, Model model) {
        User user = currentUser.getUser();
        if (user != null && idComm > 0) {
            Community community = communityService.findById(idComm);
            if (community != null && community.getUserId() == user.getId()) {
                Long numberMemb = memberService.countAllByCommunityId(idComm);
                model.addAttribute("idComm", idComm);
                model.addAttribute("numberMemb", numberMemb);
                return "/groups//randomGroupConfirm";
            }
        }

        return "redirect:/";
    }

    @PostMapping("/random/{idComm}")
    public String randomCircleDivision (@AuthenticationPrincipal CurrentUser currentUser,
                                        @RequestParam(value="name", required = false) String name,
                                        @RequestParam(value="number", required = false) int number,
                                        @PathVariable Long idComm,Model model){
        User user = currentUser.getUser();
        if (user != null && idComm > 0 && number > 0) {
            Community community = communityService.findById(idComm);
            if (community.getUserId() == user.getId()) {
                List<String> attendance = MemberAttr.attendance();
                List<Member> allMembers = memberService.findAllByCommunityId(idComm);
                int numberOfMembers = allMembers.size();
                List<Member> singleMembers = new ArrayList<>();
                List<Member> wifeMembers = new ArrayList<>();
                Group group = new Group();
                group.setIdCommunity(idComm);
                group.setName(name);
                int numberOfCircles = number;
                if (numberOfCircles < numberOfMembers) {
                    groupService.save(group);
                    if (allMembers != null) {
                        for (Member member : allMembers) {
                            boolean hasHusband = false;
                            if (member.getSex() == 'K' && member.getMarried() > 0) {
                                for (Member member1 : allMembers) {
                                    if (member1.getMarried() == member.getId()) {
                                        hasHusband = true;
                                        break;
                                    }
                                }
                            }
                            if (hasHusband) {
                                wifeMembers.add(member);
                            } else {
                                singleMembers.add(member);
                            }
                        }

                    }


                    // we have two lists - single and wifes.

                    if (singleMembers != null && singleMembers.size() > 0) {
                        List<Member> oftenMember = new ArrayList<>();
                        List<Member> rareMember = new ArrayList<>();
                        List<Member> neverMember = new ArrayList<>();
                        for (Member member : singleMembers) {
                            if (member.getAttendance().equals(attendance.get(0))) {
                                oftenMember.add(member);
                            } else if (member.getAttendance().equals(attendance.get(1))) {
                                rareMember.add(member);
                            } else {
                                neverMember.add(member);
                            }
                        }
                        // We have 3 groups of single members. Now algorythm
                        // 1. we shuffle it.
                        Collections.shuffle(oftenMember);
                        Collections.shuffle(rareMember);
                        Collections.shuffle(neverMember);
                        allMembers.clear();
                        allMembers.addAll(oftenMember);
                        allMembers.addAll(rareMember);
                        allMembers.addAll(neverMember);
                        Circle[] circles = new Circle[numberOfCircles];
                        boolean[] miss = new boolean[numberOfCircles];
                        int i = 0;
                        Iterator memb = allMembers.iterator();
                        while (memb.hasNext()) {
                            Member member = (Member) memb.next();
                            if (miss[i]) {
                                i++;
                                i = i % numberOfCircles;
                                miss[i] = false;
                            }
                            if (circles[i] == null) {
                                circles[i] = new Circle();
                            }
                            circles[i].getMembers().add(member);
                            if (member.getMarried() > 0 && wifeMembers.size() > 0) {
                                Member wife = new Member();
                                for (Member member1 : wifeMembers) {
                                    if (member.getMarried() == member1.getId()) {
                                        wife = member1;
                                        break;
                                    }
                                }
                                circles[i].getMembers().add(wife);
                                miss[i] = true;
                            }
                            i++;
                            i = i % numberOfCircles;
                        }

                        for (int j = 0; j < circles.length; j++) {
                            if (circles[j] != null) {
                                circles[j].setGroupId(group.getId());
                                circles[j].setNumber(j + 1);
                                if (circles[j].getMembers().get(0).getId() > 0) {
                                    circles[j].setResponsible(circles[j].getMembers().get(0).getId());
                                }
                                circleService.save(circles[j]);
                            }
                        }
                        return "redirect:/admin/groups/" + idComm;
                    }
                }
            }
        }
        return "redirect:/";
    }




    // *** checks if user has right to change anything in the "circle".
    // *** additionally confirms that community, group and circle are not empty and have not empty id to parent.
    private boolean checkAminCredential (User user, Long idGroup) {
        boolean credential = false;
        if (user != null && idGroup >0) {
            Group group = groupService.findById(idGroup);
            if (group != null && group.getIdCommunity() > 0) {
                Community community = communityService.findById(group.getIdCommunity());
                if (community != null && community.getUserId().equals(user.getId())) {
                    credential = true;
                }
            }
        }
        return credential;
    }
}
