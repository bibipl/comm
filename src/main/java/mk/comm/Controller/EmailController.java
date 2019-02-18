package mk.comm.Controller;

import mk.comm.Circle.Circle;
import mk.comm.Event.Event;
import mk.comm.Member.Member;
import mk.comm.Service.CircleService;
import mk.comm.Service.EventService;
import mk.comm.User.CurrentUser;
import mk.comm.User.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Email;
import java.util.List;

@Controller
@RequestMapping ("/admin/email")
public class EmailController {

    @Autowired
    CircleService circleService;
    @Autowired
    EventService eventService;

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
                                                    @RequestParam(value = "mailIds", required = false) long[] mailIds
                                                    ) {
        // TODO check credentials !!!!
        // TODO format text to html
        // TODO prepare table of members in html and add to text;
        // TODO prepare list of evens in html and add to text
        // TODO send

        return "/";
    }

}
