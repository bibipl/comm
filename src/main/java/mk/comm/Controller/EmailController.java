package mk.comm.Controller;

import mk.comm.Circle.Circle;
import mk.comm.Community.Community;
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

    @GetMapping ("/circleConsist/{idCircle}")
    public String emailToCirclewithMembers (@AuthenticationPrincipal CurrentUser currentUser,
                                           @PathVariable Long idCircle, Model model) {

            User user = currentUser.getUser();
            if ( user != null && user.getId() >0 && idCircle >0) { // check credentials !!!
                Circle circle = circleService.findById(idCircle);
                if (circle != null && circle.getMembers() != null) {
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
            if (circle != null && circle.getGroupId() > 0) {
                Group group = groupService.findById(circle.getGroupId());
                if (group != null && group.getIdCommunity() > 0) {
                    Community community = communityService.findById(group.getIdCommunity());
                    if (community.getUserId() == user.getId()) {
                        List<Event> events = eventService.findAllByCircleIdOrderByDate(idCircle);
                        String sentBy = "Email wysłany przez : " + currentUser.getUsername();
                        Context context = new Context();
                        context.setVariable("circleNumber", circle.getNumber());
                        context.setVariable("description", email.getEmailText());
                        context.setVariable("sentBy", sentBy);
                        context.setVariable("members", circle.getMembers());
                        context.setVariable("events", events);
                        String body = templateEngine.process("/email/templateMailToCircle", context);
                        String subject;
                        if (community.getName() != null) {
                            subject = community.getName();
                        } else {
                            subject = "Email wspólnotowy";
                        }
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

}
