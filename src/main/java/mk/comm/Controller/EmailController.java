package mk.comm.Controller;

import mk.comm.Circle.Circle;
import mk.comm.Community.Community;
import mk.comm.Comparators.MemberComparator;
import mk.comm.Event.Event;
import mk.comm.Group.Group;
import mk.comm.Member.Member;
import mk.comm.Service.*;
import mk.comm.User.CurrentUser;
import mk.comm.User.User;
import mk.comm.Email.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping ("/admin/email")
public class EmailController {
    @Autowired
    CommunityService communityService;
    @Autowired
    GroupService groupService;
    @Autowired
    CircleService circleService;
    @Autowired
    EventService eventService;
    @Autowired
    MemberService memberService;
    @Autowired
    EmailSender emailSender;
    @Autowired
    TemplateEngine templateEngine;
    @Autowired
    MemberController memberController;

    @GetMapping("/email/{idMemb}")
    public String sendEmailForm (@AuthenticationPrincipal CurrentUser currentUser, @PathVariable Long idMemb, Model model) {
        User user = currentUser.getUser();
        if (idMemb >0) {
            Member member = memberService.findById(idMemb);
            if (member != null && memberController.checkUserRightsToModifyMember(user, member)) {
                Community community = new Community();
                if (member.getCommunityId() >0) {
                    community = communityService.findById(member.getCommunityId());
                }
                if (community == null || community.getName() == null) {
                    community.setName("Brak nazwy wspólnoty");
                }
                if (member.getEmail() != null && !member.getEmail().equals("")) {
                    Email email = new Email();
                    email.setEmailTo(member.getEmail());
                    model.addAttribute("iam",user);
                    model.addAttribute("community", community);
                    model.addAttribute("email", email);
                    model.addAttribute("member", member);
                    return "/email/emailToOne";
                }
                if (member.getCommunityId() > 0) {
                    return "redirect:/admin/communities/view/" + member.getCommunityId();
                }
            }
        }
        return "redirect:/admin/communities";
    }
    @PostMapping("/email/{idMemb}")
    public String sendEmailAction (@AuthenticationPrincipal CurrentUser currentUser,
                                   @PathVariable Long idMemb, @ModelAttribute Email email) {
        User user = currentUser.getUser();
        String userFullName = null;
        if (user != null && user.getName() != null) {
            userFullName = user.getName();
            if (user.getSurname() != null) {
                userFullName = userFullName + " " + user.getSurname();
            }
        }
        Community community = null;
        String communityName = null;
        Member member = null;
        if (idMemb >0) {
            member = memberService.findById(idMemb);
            if (member != null && member.getId() >0 && memberController.checkUserRightsToModifyMember(user, member)) {
                String emailMemb = member.getEmail();
                String memberFullname = member.getName();
                if (memberFullname != null) {
                    if (member.getSurname() != null) {
                        memberFullname = memberFullname + ' ' + member.getSurname();
                    }
                }
                if (member.getCommunityId() > 0) {
                    community = communityService.findById(member.getCommunityId());
                    communityName = community.getName();
                }
                String sentBy = "Email wysłany przez : ";
                if (userFullName != null) {
                    sentBy = sentBy + userFullName + " (" + user.getUsername() + ")";
                }
                if (emailMemb != null && emailMemb != "" && emailMemb.equals(email.getEmailTo())) {
                    Context context = new Context();
                    context.setVariable("header", email.getEmailHead());
                    context.setVariable("title", "Cześć " + member.getName() + " !");
                    context.setVariable("description", email.getEmailText());
                    context.setVariable("sentBy", sentBy);
                    String body = templateEngine.process("templateMail", context);
                    emailSender.sendEmail(email.getEmailTo(), communityName, body);
                    if (email.isSelfSend()) {
                        emailSender.sendEmail(user.getUsername(),communityName, body);

                    }
                }
                if (member.getCommunityId() >0) {
                    return "redirect:/admin/communities/view/" + member.getCommunityId();
                }
            }
        }
        return "redirect:/admin/communities";
    }

    @GetMapping("/emailToAll/{idComm}")
    public String emailAll (@AuthenticationPrincipal CurrentUser currentUser,
                            @PathVariable Long idComm, Model model) {
        User user = currentUser.getUser();
        if ( user != null && user.getId() >0 && idComm >0) {
            Community community = communityService.findById(idComm);
            if (community != null && community.getUserId() > 0) {
                if (user.getId() == community.getUserId()) {
                    // here everything seems ok, we can start
                    List<Member> members = memberService.findAllByCommunityId(community.getId());
                    members.sort(new MemberComparator());
                    for (Member member : members) {
                        member.setDoSomeAction(true);
                    }
                    model.addAttribute("iam",user);
                    model.addAttribute("members", members);
                    model.addAttribute("community", community);
                    return "/email/sendMemberEmailSome";
                }
            }
        }
        return "redirect:/admin/communities";
    }
    @GetMapping("/emailToSome/{idComm}")
    public String emailSome (@AuthenticationPrincipal CurrentUser currentUser,
                             @PathVariable Long idComm, Model model) {
        User user = currentUser.getUser();
        if ( user != null && user.getId() >0 && idComm >0) {
            Community community = communityService.findById(idComm);
            if (community != null && community.getUserId() > 0) {
                if (user.getId() == community.getUserId()) {
                    // here everything seems ok, we can start
                    List<Member> members = memberService.findAllByCommunityId(community.getId());
                    members.sort(new MemberComparator());
                    for (Member member : members) {
                        member.setDoSomeAction(false);
                    }
                    model.addAttribute("iam",user);
                    model.addAttribute("members", members);
                    model.addAttribute("community", community);
                    return "/email/sendMemberEmailSome";
                }
            }
        }
        return "redirect:/admin/communities";
    }

    @PostMapping("/emailToSome/{idComm}")
    public String sendEmaillToMany(@AuthenticationPrincipal CurrentUser currentUser,
                                   @PathVariable Long idComm,
                                   @RequestParam(value = "mailIds", required = false) long[] mailIds,
                                   @RequestParam(value = "emailHead", required = false) String emailHead,
                                   @RequestParam(value = "emailText", required = false) String emailText){

        User user = currentUser.getUser();
        if (user != null) {
            if (emailHead == null) emailHead = "Brak Tematu";
            String sentBy = "Email wysłany przez : ";
            Community community;
            String communityName = "";
            if (user.getName() != null) {
                sentBy = sentBy + user.getName();
                if (user.getSurname() != null) {
                    sentBy = sentBy + " " + user.getSurname();
                }
            }
            if (idComm > 0) {
                community = communityService.findById(idComm);
                if (community != null && community.getUserId() == user.getId()) {
                    communityName = community.getName();

                    if (mailIds != null && mailIds.length > 0) {
                        for (int i = 0; i < mailIds.length; i++) {
                            if (mailIds[i] > 0) {
                                // set full name
                                Member member = memberService.findById(mailIds[i]);
                                if (member != null && member.getCommunityId() == idComm) {
                                    String emailMemb = member.getEmail();
                                    String memberFullname = member.getName();
                                    if (memberFullname != null && member.getSurname() != null) {
                                        memberFullname = memberFullname + ' ' + member.getSurname();
                                    }

                                    if (emailMemb != null && emailMemb != "") {
                                        Context context = new Context();
                                        context.setVariable("header", "Email wspólnotowy. " + communityName);
                                        context.setVariable("title", "Witaj " + memberFullname + " !");
                                        context.setVariable("description", emailText);
                                        context.setVariable("sentBy", sentBy);
                                        String body = templateEngine.process("templateMail", context);
                                        emailSender.sendEmail(emailMemb, emailHead, body);
                                    }
                                }
                            }
                        }
                    }
                    return "redirect:/admin/communities/view/" + idComm;
                }
            }
            return "redirect:/admin/communities";
        }
        return "redirect:/";
    }

    @GetMapping("/circleNews/{idCircle}")
    public String emailCircleNews (@AuthenticationPrincipal CurrentUser currentUser,
                            @PathVariable Long idCircle, Model model) {
        User user = currentUser.getUser();
        if ( user != null && user.getId() >0 && idCircle >0) {
            Circle circle = circleService.findById(idCircle);
            if (circle != null && circle.getGroupId() > 0) {
                Group group = groupService.findById(circle.getGroupId());
                if (group != null && group.getIdCommunity() > 0) {
                    Community community = communityService.findById(group.getIdCommunity());
                    if (community != null && community.getUserId() > 0 && user.getId() == community.getUserId()) {
                        // here everything seems ok, we can start
                        List<Member> members = circle.getMembers();
                        members.sort(new MemberComparator());
                        for (Member member : members) {
                            member.setDoSomeAction(true);
                        }
                        model.addAttribute("iam", user);
                        model.addAttribute("members", members);
                        model.addAttribute("community", community);
                        return "/email/sendMemberEmailSome";
                    }
                }
            }
        }
        return "redirect:/admin/communities";
    }
    // Send email with inhalt of the circle (circle members) to the
    @GetMapping ("/circleConsist/{idCircle}")
    public String emailToCirclewithMembers (@AuthenticationPrincipal CurrentUser currentUser,
                                           @PathVariable Long idCircle, Model model) {

            User user = currentUser.getUser();
            if ( user != null && user.getId() >0 && idCircle >0) { // check credentials !!!
                Circle circle = circleService.findById(idCircle);
                if (circle != null && circle.getMembers() != null) {
                    circle = Circle.SortByName(circle);
                    for (Member member : circle.getMembers()) {
                            member.setDoSomeAction(true);
                    }
                    List<Event> events = eventService.findAllByCircleIdOrderByDate(idCircle);
                    model.addAttribute("iam",user);
                    model.addAttribute("circle", circle);
                    model.addAttribute("events", events);
                    return "/email/emailMembersToCircle";
                }
            }
     return "/email/emailMembersToCircle/{idCircle}"; /// here - return to circles.
    }
    @PostMapping ("circleConsist/{idCircle}")
    public  String emailToCirclewithMembersAction ( @AuthenticationPrincipal CurrentUser currentUser,
                                                    @ModelAttribute Email email,
                                                    @PathVariable Long idCircle,
                                                    @RequestParam(value = "mailIds", required = false) long[] mailIds,
                                                    @RequestParam(value="selfSend", required = false) boolean selfSend
                                                    ) {
        User user = currentUser.getUser();
        if (user != null && idCircle >0) {
            Circle circle = circleService.findById(idCircle);
            circle = Circle.SortByName(circle);
            if (circle != null && circle.getGroupId() > 0) {
                Group group = groupService.findById(circle.getGroupId());
                if (group != null && group.getIdCommunity() > 0) {
                    Community community = communityService.findById(group.getIdCommunity());
                    if (community.getUserId() == user.getId()) {
                        List<Event> events = eventService.findAllByCircleIdOrderByDate(idCircle);
                        String sentBy = "Email wysłany przez : " + currentUser.getUsername();
                        Context context = new Context();
                        context.setVariable("circle", circle);
                        context.setVariable("description", email.getEmailText());
                        context.setVariable("sentBy", sentBy);
                        context.setVariable("events", events);
                        String body = templateEngine.process("/email/templateMailToCircle", context);
                        String subject;
                        if (community.getName() != null) {
                            subject = community.getName();
                        } else {
                            subject = "Email wspólnotowy";
                        }
                        mailToThem(mailIds, subject, body);
                        if (selfSend && user.getUsername() != null)  {
                            emailSender.sendEmail(user.getUsername(), subject, body);

                        }

                    }
                    if (group.getId() > 0)
                        return "redirect:/admin/groups/view/" + group.getId();
                }

            }
        }
        return "redirect:/";
    }

    @GetMapping ("/group/{idGroup}")
    public String emailToGroupwithCircleMembers (@AuthenticationPrincipal CurrentUser currentUser,
                                            @PathVariable Long idGroup, Model model) {
        User user = currentUser.getUser();
        if ( user != null && user.getId() >0 && idGroup >0) { // check credentials !!!
            Group group = groupService.findById(idGroup);
            if (group != null && group.getIdCommunity() >0) {
                Community community = communityService.findById(group.getIdCommunity());
                if (community != null && community.getUserId() == user.getId()) {
                    List<Circle> circles = circleService.findAllByGroupIdOrderByNumberAsc(idGroup);
                    if (circles != null) {
                        List<Member> members = new ArrayList<>();
                        for (Circle circle : circles) {
                            if (circle.getMembers() != null) {
                                members.addAll(circle.getMembers());
                            }
                        }
                        if (members != null) {
                            members.sort(new MemberComparator());
                            for (Member member : members) {
                                member.setDoSomeAction(true);
                            }
                            model.addAttribute("group", group);
                            model.addAttribute("members", members);
                            return "/email/toMembersForm";
                        }
                    }

                }
            }
        }
        return "redirect:/";
    }

    @PostMapping ("/group/{idGroup}")
    public String emailToGroupwithCircleMembersAction (@AuthenticationPrincipal CurrentUser currentUser,
                                                       @RequestParam(value = "mailIds", required = false) long[] mailIds,
                                                       @RequestParam(value = "emailText", required = false) String emailText,
                                                       @PathVariable Long idGroup, Model model) {
        User user = currentUser.getUser();
        if ( user != null && user.getId() >0 && idGroup >0) { // check credentials !!!
            Group group = groupService.findById(idGroup);
            if (group != null && group.getIdCommunity() >0) {
                Community community = communityService.findById(group.getIdCommunity());
                if (community != null && community.getUserId() == user.getId()) {
                    List<Circle> circles = circleService.findAllByGroupIdOrderByNumberAsc(idGroup);
                    if (circles != null) {
                        String sentBy = "Email wysłany przez : " + currentUser.getUsername();
                        for (Circle circle : circles) {
                            if (circle.getId() >0) {
                                circle.setEvents(eventService.findAllByCircleIdOrderByDate(circle.getId()));
                            }
                        }
                        String subject = "";
                        if (community.getName() != null) {
                            subject = community.getName();
                        } else {
                            subject = "Email wspólnotowy";
                        }
                        Context context = new Context();
                        context.setVariable("circles", circles);
                        context.setVariable("subject", subject);
                        context.setVariable("description", emailText);
                        context.setVariable("sentBy", sentBy);
                        String body = templateEngine.process("/email/templateMailToGroup", context);

                        mailToThem(mailIds, subject, body);
                    }
                }
                if (group.getId() > 0) return "redirect:/admin/groups/view/" + group.getId();
            }
        }
        return "redirect:/";
    }

    //***********************************************************************************************************
    // send mails to the members of id's in the table mailIds
    private void mailToThem(@RequestParam(value = "mailIds", required = false) long[] mailIds, String subject, String body) {
        if (mailIds != null && mailIds.length > 0) {
            for (Long i : mailIds) {
                if (i > 0) {
                    Member member = memberService.findById(i);
                    if (member.getEmail() != null) {
                        emailSender.sendEmail(member.getEmail(), subject, body);
                    }
                }
            }
        }
    }

}
