package mk.comm.Controller;

import mk.comm.Community.Community;
import mk.comm.Email;
import mk.comm.Member.Member;
import mk.comm.Member.MemberAttr;
import mk.comm.Role.Role;
import mk.comm.Service.CommunityService;
import mk.comm.Service.EmailSender;
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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.jws.soap.SOAPBinding;
import javax.validation.Valid;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

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

    // ** Edit community. We go user and comm id and return user and community) ***/
    @GetMapping("/community/edit/{idComm}")
    public String communityEditForm (@AuthenticationPrincipal CurrentUser currentUser,
                                     @PathVariable Long idComm, Model model){
        User user = currentUser.getUser();
        Community community = communityService.findById(idComm);
        if (user != null && user.getId() > 0) {
            if(community != null && community.getId() > 0 && community.getUserId() == user.getId()) {
                model.addAttribute("community", community);
                model.addAttribute("iam", user);
                return "/admin/addCommunity";
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

    // sets model at proper data ready to go to member Add  - model contains member, community, iam,attendace and sex lists***//
    @GetMapping ("/community/member/add/{idComm}")
    public String memberAdd (@AuthenticationPrincipal CurrentUser currentUser,
                             @PathVariable Long idComm, Model model){
        User user = currentUser.getUser();
        Community community = communityService.findById(idComm);
        Member member = new Member();
        if (idComm > 0) {
           community = communityService.findById(idComm);
           if (community != null && community.getUserId() == user.getId()) {
              member.setCommunityId(community.getId());
               model = addToModelMemberData(model, user, member);
              return "/admin/addMember";
           }
        }
        return "redirect:/admin/community/view/" + idComm;
    }



    @PostMapping("/community/member/add")
    public String memberSave (@AuthenticationPrincipal CurrentUser currentUser,
                              @Valid @ModelAttribute Member member, BindingResult result, Model model){

        User user = currentUser.getUser();
        if (result.hasErrors()) {
            model = addToModelMemberData(model, user, member);
            return "/admin/addMember";
        }
        Long idComm = member.getCommunityId();
        boolean admHasRights = false;
        if (idComm >0) {
            List<Community> communities = communityService.findAllByUserId(user.getId());
            if (communities !=null) {
                for (Community comm : communities) {
                    if (comm.getId() == idComm) {
                        admHasRights = true;
                        member.setToken(UUID.randomUUID().toString());
                    }
                }
            }
        }
        if (admHasRights) {
            memberService.save(member); // we do a lot of efforts not to let unauthorized save.
            return "redirect:/admin/community/view/" +idComm;
        }

        return "redirect:/admin/communities";
    }


    @GetMapping("/community/member/edit/{id}")
    public String memberEdit (){
        return "";
    }


    @GetMapping("/community/member/addMarriage/{idMemb}")
    public String memberMarriage (@AuthenticationPrincipal CurrentUser currentUser,
                                  @PathVariable Long idMemb, Model model){
        User user = currentUser.getUser();
        Member member = null;
        Member member2 = null;
        Community community = null;
        if (idMemb > 0) {
            member = memberService.findById(idMemb);
            if (member != null && member.getCommunityId() >0 ) {
                community = communityService.findById(member.getCommunityId());
                if (member.getMarried() > 0) {
                    member2 = memberService.findById(member.getMarried());
                    model.addAttribute("member1", member);
                    model.addAttribute("member2", member2);
                    model.addAttribute("community", community);
                    model.addAttribute("iam", user);
                    return "admin/tearApart";

                } else {
                    List<Member> marriages = null;
                    if (member.getSex() == 'M') {
                        // get all free women
                        marriages = memberService.findAllNotMarriedBySex('K');
                    } else if (member.getSex() == 'K') {
                        // get all free men
                        marriages = memberService.findAllNotMarriedBySex('M');
                    } else {
                        if (community.getId() >0)return "redirect:admin/community/view/community/"+community.getId();
                        else return "redirect:/admin/communities";
                    }
                    if (marriages != null) {
                        model.addAttribute("member", member);
                        model.addAttribute("marriages", marriages);
                        model.addAttribute("community", community);
                        model.addAttribute("iam", user);
                        return "/admin/addMarried";
                    }
                }
            }
        }
        return "";
    }

    @PostMapping("/community/member/addMarriage/{idMemb}")
    public String memberMarriageSave (@AuthenticationPrincipal CurrentUser currentUser,
                                      @PathVariable Long idMemb,
                                      @ModelAttribute Member member){
        String atm = null;
        return "";
    }
    @GetMapping("/community/member/view/{id}")
    public String memberView (){
        return "";
    }

    @GetMapping("/community/member/email/{idMemb}")
    public String sendEmailForm (@AuthenticationPrincipal CurrentUser currentUser, @PathVariable Long idMemb, Model model) {
        User user = currentUser.getUser();
        if (idMemb >0) {
            Member member = memberService.findById(idMemb);
            if (member != null) {
                Community community = new Community();
                if (member.getCommunityId() >0) {
                    community = communityService.findById(member.getCommunityId());
                }
                if (community == null || community.getName() == null) {
                    community.setName("Brak nazwy wspólnoty");
                }
                Email email = new Email();
                email.setEmailTo(member.getEmail());
                if (email.getEmailTo() != null && email.getEmailTo() != "") {
                    model.addAttribute("iam",user);
                    model.addAttribute("community", community);
                    model.addAttribute("email", email);
                    model.addAttribute("member", member);
                    return "/email/emailToOne";
                }
                if (member.getCommunityId() > 0) {
                    return "redirect:/admin/communit/view/" + member.getCommunityId();
                }
            }
        }
      return "redirect:/admin/communities";
    }
    @PostMapping("/community/member/email/{idMemb}")
    public String sendEmailAction (@AuthenticationPrincipal CurrentUser currentUser,
                                   @PathVariable Long idMemb, @ModelAttribute Email email) {
        User user = currentUser.getUser();
        Community community = null;
        String communityName = null;
        Member member = null;
        if (idMemb >0) {
           member = memberService.findById(idMemb);
            if (member != null && member.getId() >0) {
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
                if (memberFullname != null) {
                   sentBy = sentBy + memberFullname;
                }
                if (emailMemb != null && emailMemb != "" && emailMemb.equals(email.getEmailTo())) {
                    sentBy = sentBy + " (" + emailMemb + ")";
                    Context context = new Context();
                    context.setVariable("header", "Email wspólnotowy");
                    context.setVariable("title", "Witaj " + member.getName() + ' ' + member.getSurname() + "!");
                    context.setVariable("description", email.getEmailText());
                    context.setVariable("sentBy", sentBy);
                    String body = templateEngine.process("templateMail", context);
                    emailSender.sendEmail(email.getEmailTo(), communityName, body);
                   if (email.isSelfSend()) {
                       emailSender.sendEmail(user.getUsername(),communityName, body);

                   }
                }
                if (member.getCommunityId() >0) {
                    return "redirect:/admin/community/view/" + member.getCommunityId();
                }
            }
        }
        return "redirect:/admin/communities";
    }

    // ****sets model at proper data ready to go to member Add  - model contains member, community, iam,attendace and sex lists***//
    private Model addToModelMemberData (Model model, User user, Member member) {

        Community community;
        List<String> attendance = MemberAttr.attendance();
        List<Character> sex = MemberAttr.sex();
        List<String> married = MemberAttr.married();
        Long idComm = member.getCommunityId();
        if (idComm > 0) {
            community = communityService.findById(idComm);
            if (community != null && community.getUserId() == user.getId()) {
                member.setCommunityId(community.getId());
                model.addAttribute("member", member);
                model.addAttribute("community", community);
                model.addAttribute("iam", user);
                model.addAttribute("attendance", attendance);
                model.addAttribute("sex", sex);
                model.addAttribute("married", married);
            }
        }

        return model;
    }
}
