package mk.comm.Controller;

import mk.comm.Circle.Circle;
import mk.comm.Community.Community;
import mk.comm.Group.Group;
import mk.comm.Member.Member;
import mk.comm.Role.Role;
import mk.comm.Service.*;
import mk.comm.User.CurrentUser;
import mk.comm.User.User;
import mk.comm.verificationToken.VerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    UserService userService;
    @Autowired
    CommunityService communityService;
    @Autowired
    GroupService groupService;
    @Autowired
    CircleService circleService;
    @Autowired
    MemberService memberService;
    @Autowired
    EmailSender emailSender;
    @Autowired
    TemplateEngine templateEngine;
    @Autowired
    VerificationTokenService verificationTokenService;


    //************* basic for admin after loging ***********************//
    //*** if you are here means you have your own community to rule ****//
    //*** so show your resources ***************************************//
    //*** **************************************************************//
    @GetMapping("/")
    public String adminStart(@AuthenticationPrincipal CurrentUser currentUser, Model model) {
        User user = currentUser.getUser();

        for (Role role : user.getRoles()) {
            if (role.getName().equals("ROLE_ADMIN")) {
                model.addAttribute("iam", user);
                return "/admin/start";
            }
        }
        return "/landing";
    }
    //************************************************//
    //***  here admin wants to update admin's data  ***//
    //************************************************//

    @GetMapping("/editAdmin")
    public  String editSelfAdmin (@AuthenticationPrincipal CurrentUser currentUser, Model model) {
        User user = currentUser.getUser();
        if (user != null && user.getId() != 0) {
            model.addAttribute("iam", user);
            return "/admin/editAdmin";
        }
        return"/landing";
    }
    // *** here we change only name and surname and eventually other adnmins' personal data ********//
    // *** username (email) and password - we change in other get/post method) *** //
    @PostMapping("/editAdmin")
    public  String editSelfAdminAction (@AuthenticationPrincipal CurrentUser currentUser, @ModelAttribute User iam, Model model) {

        if (iam == null || iam.getName() == null || iam.getSurname() == null || iam.getName() == "" || iam.getSurname() == "") {
            model.addAttribute("iam", iam);
            return "/admin/editAdmin";
        }
        User userAdm = currentUser.getUser();
        if (userAdm != null && userAdm.getId() != 0 && iam != null && iam.getId() != 0 && userAdm.getId() == iam.getId()) {
            User userCheck = userService.findById(iam.getId());
            boolean change = false;
            if (!iam.getName().equals(userCheck.getName())) {
                userCheck.setName(iam.getName());
                change = true;
            }
            if (!iam.getSurname().equals(userCheck.getSurname())) {
                userCheck.setSurname(iam.getSurname());
                change = true;

            }
            if (change) {
                userService.saveUser(userCheck);
                return "redirect:/";
            }
        }
        return"/";
    }

    //******************************************************************//
    //***  here admin wants to delete admin's data  *******************//
    //****we want to be sure all the related data are also deleted ***//
    //***************************************************************//

    @GetMapping("/deleteAdmin")
    public  String deleteSelfAdmin (@AuthenticationPrincipal CurrentUser currentUser, Model model) {
        // ** we want to show name, number of communities and overall number of members to warn befor delete ***//
        User user = currentUser.getUser();
        if (user != null && user.getId() != 0) {
            List<Community> communities= communityService.findAllByUserId(user.getId());
            if (communities != null && communities.size() > 0) {
                long numberOfMembers = 0;
                for (Community community : communities) {
                    if (community != null && community.getId() != 0) {
                        numberOfMembers += memberService.countAllByCommunityId(community.getId());
                    }
                    model.addAttribute("numberOfMembers", numberOfMembers);
                    model.addAttribute("numberOfComm", communities.size());
                    model.addAttribute("iam", user);
                    return"/admin/deleteAdmin";
                }
            }
        }
        return "redirect:/";
    }
    // ** here we have to delete user and all his communities and all groups for each community and all circels for each group
    // ** and all members
    @PostMapping("/deleteAdmin")
    public  String deleteSelfAdminAction (@AuthenticationPrincipal CurrentUser currentUser) {
        User user = currentUser.getUser();
        if (user != null && user.getId() != 0) {
            List<Community> communities = communityService.findAllByUserId(user.getId());
            if (communities != null) {
                for (Community community : communities) {
                    if (community != null && community.getId() != 0) {
                        List<Group> groups = groupService.findAllByIdCommunity(community.getId());
                        if (groups != null) {
                            for (Group group : groups) {
                                if (group != null && group.getId() != 0) {
                                    List <Circle> circles = circleService.findAllByGroupIdOrderByNumberAsc(group.getId());
                                    if (circles != null) {
                                        for (Circle circle : circles) {
                                            if (circle != null && circle.getId() != 0) {
                                                circleService.delete(circle);
                                            }
                                        }
                                    }
                                    groupService.delete(group);
                                }
                            }
                        }
                        List<Member> members = memberService.findAllByCommunityIdOrderBySurnameAscNameAsc(community.getId());
                        if (members != null) {
                            for (Member member : members) {
                                if (member != null && member.getId() != 0) {
                                    memberService.delete(member);
                                }
                            }
                        }
                        communityService.delete(community);
                    }
                }
            }
            userService.delete(user);
        }
        return"/landing";
    }

    @GetMapping ("/changeAdminEmail/{error}")
    public String changeAdminEmail (@AuthenticationPrincipal CurrentUser currentUser, Model model,
                                    @PathVariable int error) {
        //*** int error : 1- email null or ""; 2- email and check differ; 4- does not lokk as an email
        //*** we sent erroe, Srting email and empty emaicCeck
        User user = currentUser.getUser();
        if (user != null && user.getId() != null && user.getId() != 0) {
            String adminEmail = user.getUsername();
            if (adminEmail == null) adminEmail = "";
            model.addAttribute("error", error);
            model.addAttribute("adminEmail", adminEmail);
            model.addAttribute("adminEmailCheck", "");
            return ("/admin/changeAdminEmail");
        }
        return ("/admin/changeAdminEmail");
    }
    @PostMapping ("/changeAdminEmail/{error}")
    public String changeAdminEmailAction (@AuthenticationPrincipal CurrentUser currentUser,
                                          @RequestParam(value = "adminEmail", required = false) String adminEmail,
                                          @RequestParam(value = "adminEmailCheck", required = false) String adminEmailCheck,
                                          @PathVariable int error,
                                          Model model) {


        Pattern p = Pattern.compile("[^@]+@[^@]+\\.[^@]+");
        // simple regex for an email. So far, no nedd for more complex
        Matcher matcher = null;
        if (adminEmail != null) {
            matcher = p.matcher(adminEmail);
        }
        boolean emaiExistsInDatabase = false;
        if (matcher != null && matcher.find() && adminEmailCheck != null && adminEmail.equals(adminEmailCheck)) {
            User checkEmail = userService.findByUsername(adminEmail);
            if (checkEmail == null) {
                // 1. create a new token
                User user = currentUser.getUser();
                String token = UUID.randomUUID().toString();
                VerificationToken verificationToken = new VerificationToken();
                verificationToken.setUser(user);
                verificationToken.setToken(token);
                verificationToken.setAction(1);
                verificationToken.setEmail(adminEmail);
                verificationTokenService.save(verificationToken);
                // 2. create  path : admin/changemail/{userId}/{token}
                String link = "localhost:8080/admin/changeemail/" + user.getId() + "/" + token;
                // create an email with the path
                Context context = new Context();
                context.setVariable("header", "Zmiana emaila w serwisie Zarządzaj wspólnotą");
                context.setVariable("title", "Witaj " + user.getUsername() + "!");
                context.setVariable("description", "Jeśli potwierdzasz zmianę emaila, kliknij w poniższy link");
                context.setVariable("link", link);
                String body = templateEngine.process("/email/templateEmailChange", context);
                emailSender.sendEmail(adminEmail, "Zarządzaj swoją wspólnotą zmiana emaila", body);

                // send email with uuid to confirm email. After confirmation the change is done.
            } else { emaiExistsInDatabase = true;}
        } else {// else means errors - go back to the form;
            error = 0;
            if (adminEmail == null || adminEmail == "") { adminEmail = ""; error += 1;};
            if (!adminEmail.equals(adminEmailCheck)) {error += 2;}
            if (matcher != null && !matcher.find()) {error += 4;}
            if (emaiExistsInDatabase) {error += 8;}
            model.addAttribute("error", error);
            model.addAttribute("adminEmail", adminEmail);
            model.addAttribute("adminEmailCheck", "");
            return ("/admin/changeAdminEmail");
        }
        return"/";
    }
    // finally to finish email change verification of the email:
    @GetMapping ("/changeemail/{userId}/{token}")
    public String changeEmail (@AuthenticationPrincipal CurrentUser currentUser,
                               @PathVariable long userId, @PathVariable String token) {
        User user = currentUser.getUser();
        if (user != null && user.getId() == userId) {
            VerificationToken verificationToken = verificationTokenService.findByToken(token);
            if (verificationToken != null && verificationToken.getToken().equals(token)
                    && verificationToken.getUser().getId() == user.getId()) {
                User userChangeEmail = userService.findById(userId);
                userChangeEmail.setUsername(verificationToken.getEmail());
                userService.saveUser(userChangeEmail);
                verificationTokenService.delete(verificationToken);
                return "redirect:/logout";
            }
        }
        return "redirect:/";
    }

}
