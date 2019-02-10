package mk.comm.Controller;

import mk.comm.Community.Community;
import mk.comm.Email.Email;
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


    @GetMapping("/community/member/edit/{idMemb}")
    public String memberEdit (@AuthenticationPrincipal CurrentUser currentUser,
                              @PathVariable Long idMemb, Model model){
        User user = currentUser.getUser();
        if (idMemb >0) {
            Member member = memberService.findById(idMemb);
            if (member != null && member.getCommunityId() >0) {
                Community community = communityService.findById(member.getCommunityId());
                if (community != null && community.getUserId() == user.getId()) {
                    model = addToModelMemberData(model, user, member);
                    return "/admin/addMember";
                }
                if (member.getCommunityId() >0) {
                    return "redirect:/admin/community/view/" + member.getCommunityId();
                }
            }

        }
        return "redirect:/admin/communities";
    }


    @GetMapping("/community/member/addMarriage/{idMemb}")
    public String memberMarriage (@AuthenticationPrincipal CurrentUser currentUser, @PathVariable Long idMemb, Model model){
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
                    return "/admin/tearApart";

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
        return "redirect:/admin/communities";
    }

    @PostMapping("/community/member/addMarriage")
    public String memberMarriageSave (@AuthenticationPrincipal CurrentUser currentUser,
                                      @ModelAttribute Member member){
        Member member1 = null;
        if (member != null && member.getMarried() >0) {
            member1 = memberService.findById(member.getMarried());
            member = memberService.findById(member.getId());
            if (member != null && member1 != null) {
                if (member.getSex() != member1.getSex()) {
                    member.setMarried(member1.getId());
                    member1.setMarried(member.getId());
                    memberService.save(member);
                    memberService.save(member1);
                    if (member.getCommunityId() > 0) {
                        return "redirect:/admin/community/view/" + member.getCommunityId();
                    } else {
                        return "redirect:/admin/communities";
                    }
                }
            }
        }
        // ** here - no success - we go back to previous free starte *****//
        if (member1 != null) {
            member1.setMarried((long)0);
            memberService.save(member1);
        }
        if (member != null && member.getId() >0) {
            member = memberService.findById(member.getId());
            member.setMarried((long)0);
            memberService.save(member);
        }
        if (member.getCommunityId() > 0) {
            return "redirect:/admin/community/view/" + member.getCommunityId();
        } else {
            return "redirect:/admin/communities";
        }
    }

    @PostMapping("/community/member/tearMarriage")
    public String tearMarriage (@AuthenticationPrincipal CurrentUser currentUser,
                                @ModelAttribute Member member1) {
      User user = currentUser.getUser();
      Community community = null;
      Member member2 = null;
      if (member1 != null && member1.getId() > 0 && member1.getCommunityId() > 0) {
          community = communityService.findById(member1.getCommunityId());
          member1 = memberService.findById(member1.getId());
          if (member1.getMarried() >0 && community.getUserId() == user.getId()) {
              member2 = memberService.findById(member1.getMarried());
              if (member2 != null) {
                  member1.setMarried((long)0);
                  member2.setMarried((long)0);
                  memberService.save(member1);
                  memberService.save(member2);
              }
          }
          if (member1.getCommunityId() > 0) {
              return "redirect:/admin/community/view/" + member1.getCommunityId();
          }
      }
        return "redirect:/admin/communities";
    }

    @GetMapping("/community/member/view/{idMember}")
    public String memberView (@AuthenticationPrincipal CurrentUser currentUser,
                              @PathVariable Long idMember, Model model){
        if (idMember > 0) {
            User user = currentUser.getUser();
            Member member = memberService.findById(idMember);
            if (checkUserRightsToModifyMember (user, member)) {
                Community community = communityService.findById(member.getCommunityId());
                if (community != null) {
                    String marry = "";
                    if (member.getMarried() > 0) {
                        Member marriage = memberService.findById(member.getMarried());
                        if (marriage != null) {

                            if (marriage.getName() != null) marry = marriage.getName();
                            if (marriage.getSurname() != null) marry = marry + " " + marriage.getSurname();
                        }
                    }
                    model = addToModelMemberData(model, user, member);
                    model.addAttribute("community", community);
                    model.addAttribute("marry", marry);
                    return "/admin/viewMember";
                }
            }
        }
        return "";
    }
    @GetMapping("/community/member/delete/{idMember}")
    public String memberDeleteForm (@AuthenticationPrincipal CurrentUser currentUser,
                                    @PathVariable Long idMember, Model model) {
        if (idMember > 0) {
            User user = currentUser.getUser();
            Community community = null;
            Member member = memberService.findById(idMember);
            if (checkUserRightsToModifyMember (user, member)) {
                if (member.getCommunityId() > 0) {
                   community = communityService.findById(member.getCommunityId());
                }
                model.addAttribute("community", community);
                model = addToModelMemberData(model,user,member);
                return "/admin/deleteMember";

            }
            if (member.getCommunityId() > 0) {
                return "redirect:/admin/community/view/" + member.getCommunityId();
            }
        }
        return"redirect:/admin/communities";
    }
    @PostMapping("community/member/delete")
    public String memberDeleteAction(@AuthenticationPrincipal CurrentUser currentUser,
                                     @ModelAttribute Member member1) {
        User user = currentUser.getUser();
        Member member = memberService.findById(member1.getId());
        if (checkUserRightsToModifyMember (user, member)) {
            if (member.getMarried() != 0) {
                Member memberMarried = memberService.findById(member.getMarried());
                if (memberMarried != null) {
                    memberMarried.setMarried((long)0);
                    memberService.save(memberMarried);
                }
            }
            memberService.delete(member);

            }
        if (member1.getCommunityId() > 0) {
            return "redirect:/admin/community/view/" + member1.getCommunityId();
        }
        return "redirect:/admin/communities";
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
                    return "redirect:/admin/community/view/" + member.getCommunityId();
                }
            }
        }
      return "redirect:/admin/communities";
    }
    @PostMapping("/community/member/email/{idMemb}")
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
                if (userFullName != null) {
                   sentBy = sentBy + userFullName;
                }
                if (emailMemb != null && emailMemb != "" && emailMemb.equals(email.getEmailTo())) {
                    sentBy = sentBy + " (" + user.getUsername() + ")";
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
    @GetMapping("/community/member/emailToAll/{idComm}")
    public String emailAll (@AuthenticationPrincipal CurrentUser currentUser,
                            @PathVariable Long idComm, Model model) {
        User user = currentUser.getUser();
        if ( user != null && user.getId() >0 && idComm >0) {
            Community community = communityService.findById(idComm);
            if (community != null && community.getUserId() > 0) {
                if (user.getId() == community.getUserId()) {
                    // here everything seems ok, we can start
                    List<Member> members = memberService.findAllByCommunityId(community.getId());
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
    @GetMapping("/community/member/emailToSome/{idComm}")
        public String emailSome (@AuthenticationPrincipal CurrentUser currentUser,
                                 @PathVariable Long idComm, Model model) {
        User user = currentUser.getUser();
        if ( user != null && user.getId() >0 && idComm >0) {
            Community community = communityService.findById(idComm);
            if (community != null && community.getUserId() > 0) {
                if (user.getId() == community.getUserId()) {
                    // here everything seems ok, we can start
                    List<Member> members = memberService.findAllByCommunityId(community.getId());
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

    @PostMapping("/community/member/emailToSome/{idComm}")
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
                if (community != null) {
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
                    return "redirect:/admin/community/view/" + idComm;
                }
            }
            return "redirect:/admin/communities";
        }
        return "redirect:/";
    }



    // *** here we check credentioals - if user can modify member
    // ** check if USER exists is not null and has id >0 alo if MEMBER exists and has community info
    private boolean checkUserRightsToModifyMember (User user, Member member) {
        boolean right = false;
        // check if member is not null and has community id information
        if (user != null && user.getId() >0 && member != null && member.getId() > 0 && member.getCommunityId() > 0) {
            // now load community to ger community id
            Community community = communityService.findById(member.getCommunityId());
            // check if community not null and if has information about user (owner of community member belongs to)
            if (community != null && community.getUserId() >0) {
                if (community.getUserId() == user.getId()) {
                    right = true;
                }
            }
        }
        return right;
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
