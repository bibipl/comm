package mk.comm.Controller;

import mk.comm.Circle.Circle;
import mk.comm.Community.Community;
import mk.comm.Event.Event;
import mk.comm.Event.ServEvent;
import mk.comm.Group.Group;
import mk.comm.Service.CircleService;
import mk.comm.Service.CommunityService;
import mk.comm.Service.EventService;
import mk.comm.Service.GroupService;
import mk.comm.User.CurrentUser;
import mk.comm.User.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@Controller
@RequestMapping("/admin/events")
public class EventController {

    @Autowired
    EventService eventService;
    @Autowired
    CircleService circleService;
    @Autowired
    GroupService groupService;
    @Autowired
    CommunityService communityService;


// *** Return to list of circlers within specified group thet the CIRCLE belongs to :
    @GetMapping ("/{idCircle}")
    public String showGroupCircle (@AuthenticationPrincipal CurrentUser currentUser,
                                   @PathVariable Long idCircle) {
        User user = currentUser.getUser();
        Long idC = idCircle;
        if (checkCredentials(user, idC)) {
            return returnToCircleList(idCircle);
        }
        return "redirect:/";
    }

    @GetMapping("/add/{idCircle}")
    public String addEvent(@AuthenticationPrincipal CurrentUser currentUser,
                           @PathVariable Long idCircle, Model model) {
        User user = currentUser.getUser();
        if (checkCredentials(user, idCircle)) {
            String username = userFullName(user);
            List<String> eventNames = ServEvent.eventList();
            Event event = new Event();
            event.setCircleId(idCircle);
            event.setDate(LocalDate.now());
            model.addAttribute("event", event);
            model.addAttribute("eventNames", eventNames);
            model.addAttribute("userName", username);
            return "/event/addEvent";
        }
        return "redirect:/admin/communities";
    }

    @GetMapping("/editEvent/{idEvent}")
    public String editEvent(@AuthenticationPrincipal CurrentUser currentUser,
                           @PathVariable Long idEvent, Model model) {
        User user = currentUser.getUser();
        Event event = new Event();
        if (idEvent > 0) {
            event = eventService.findById(idEvent);
            if (event != null && event.getCircleId() > 0 && checkCredentials(user, event.getCircleId())) {
                String username = userFullName(user);
                model.addAttribute("event", event);
                model.addAttribute("eventNames", ServEvent.eventList());
                model.addAttribute("userName", username);
                return "/event/addEvent";
            }
        }
        return "redirect:/admin/communities";
    }

    @PostMapping("/add")
    public String addEventAction (@AuthenticationPrincipal CurrentUser currentUser,
                                  @ModelAttribute Event event, BindingResult result) {
        User user = currentUser.getUser();
        if (event != null && event.getCircleId() >0 && checkCredentials(user, event.getCircleId())) {
            if (event.getDescription() == null || event.getDescription() == "") {
                event.setDescription("Brak opisu.");
            }
            eventService.save(event);
            return returnToCircleList(event.getCircleId());
        }
        return"redirect:/";
    }

    @GetMapping("/deleteEvent/{idEvent}")
    public String deleteEvent(@AuthenticationPrincipal CurrentUser currentUser,
                           @PathVariable Long idEvent, Model model) {
        User user = currentUser.getUser();
        Event event = new Event();
        if (idEvent > 0) {
            event = eventService.findById(idEvent);
            if (event != null && event.getCircleId() > 0 && checkCredentials(user, event.getCircleId())) {
                        model.addAttribute("event", event);
                        return "/event/deleteEvent";
            }
        }
        return "redirect:/admin/communities";
    }

    @PostMapping("/deleteEvent")
    public String deleteEventAction (@AuthenticationPrincipal CurrentUser currentUser,
                                     @ModelAttribute Event event, BindingResult result) {
        User user = currentUser.getUser();
        if (event != null && event.getCircleId() >0 && checkCredentials(user, event.getCircleId())) {
            Long circleId = event.getCircleId();
            eventService.delete(event);
            return returnToCircleList(circleId);
        }
        return"redirect:/";
    }



    private boolean checkCredentials(User user, Long idCircle) {
        boolean credentials = false;

        if (user.getId() > 0 && idCircle > 0) {

            Circle circle = circleService.findById(idCircle);
            if (circle != null && circle.getGroupId() > 0) {
                Group group = groupService.findById(circle.getGroupId());
                if (group != null && group.getIdCommunity() > 0) {
                    Community community = communityService.findById(group.getIdCommunity());
                    if (community != null && community.getUserId() == user.getId()) ;
                    credentials = true;
                }

            }

        }
        return credentials;
    }

    private String userFullName (User user) {
        String fullName = "";
        if (user != null) {
            if (user.getName() != null) {
                fullName = user.getName();
            }
            if (user.getSurname() != null) {
                if (fullName != "") {
                    fullName = fullName + " " + user.getSurname();
                } else {
                    fullName = user.getSurname();
                }
            }
            return fullName;
        }
        return null;
    }

    private String returnToCircleList(Long idCircle) {
        if (idCircle > 0) {
            Circle circle = circleService.findById(idCircle);
            if (circle != null && circle.getGroupId() > 0) {
                Group group = groupService.findById(circle.getGroupId());
                if (group != null) {
                    return "redirect:/admin/groups/view/" + group.getId();
                }
            }
        }
        return null;
    }

}
