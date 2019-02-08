package mk.comm.Controller;

import mk.comm.Community.Community;
import mk.comm.Email;
import mk.comm.Member.Member;
import mk.comm.Member.MemberAttr;
import mk.comm.Member.MemberList;
import mk.comm.Member.MemberMailTo;
import mk.comm.Role.Role;
import mk.comm.Service.CommunityService;
import mk.comm.Service.EmailSender;
import mk.comm.Service.MemberService;
import mk.comm.Service.UserService;
import mk.comm.User.CurrentUser;
import mk.comm.User.User;
import org.omg.CORBA.COMM_FAILURE;
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
                members = memberService.findAllByCommunityIdOrderBySurname(community.getId());
                if (members != null) {
                    members = getMarriageOrder (members);
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
        public String emailSome () {
            return"";
    }

    @PostMapping("/community/member/emailToSome/{idComm}")
    public String sendEmaillToMany(@AuthenticationPrincipal CurrentUser currentUser,
                                   @PathVariable Long idComm,
                                   @RequestParam(value = "mailIds", required = false) long[] mailIds,
                                   @RequestParam(value = "emailText", required = false) String emailText,
                                   @RequestParam(value = "emailHead", required = false) String emailHead) {

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

    private List<Member> getMarriageOrder (List<Member> members) {
        if (members != null && members.size() > 1) {
            int s = 0; // s - in case of wrong data to limit loop repeat.
            List<Member> inOrderList = new ArrayList<>();
            Member[] orderArray = members.toArray(new Member[members.size()]);
            int i=0;
            for (i=0; i<orderArray.length-1; i++) {
                if (orderArray[i] != null) {
                    Member tempMember = orderArray[i];
                    Long marriedId = tempMember.getMarried();
                    if (marriedId > 0) {
                        if (tempMember.getSex() == ('M')) { // we look for wife and add after.
                            for (int j = i + 1; j < orderArray.length; j++) {
                                if (marriedId == orderArray[j].getId()) {
                                    if (tempMember.getSex() == 'M') {
                                        inOrderList.add(tempMember);
                                        inOrderList.add(orderArray[j]);
                                    } else {
                                        inOrderList.add(orderArray[j]);
                                        inOrderList.add(tempMember);
                                    }
                                    orderArray[j] = null;
                                }
                            }
                        } else { // we have to move wife after a husband - buuble move
                            int s1 = s; // to check if husband not found we should not change anything - especially i;
                            for (int j = i; j<orderArray.length-1; j++) {
                                orderArray[j] = orderArray[j+1];
                                if (orderArray[j+1].getMarried() == tempMember.getId()) {
                                    s = s+1;
                                    orderArray[j+1] = tempMember;
                                    break; // we found a husband and placed a wife after him
                                }
                            }
                            if (s > s1) {i--;} //wife moved we have to check new memeber on the i position as "for" will soon i++;
                            // s > s1 shows, that husband was found. Other situation should not happen but who knows ?
                        }
                    } else { // not married, add to list in order without changes.
                        inOrderList.add(tempMember);
                    }
                }
                if (s > orderArray.length) break; // just a safe button not to go into infinite loop with wrong data.
                // eg all women and all married but not any husband. should not happen but who knows ????
            }
            if (orderArray[i] != null) {
                inOrderList.add(orderArray[i]);
            }
            return inOrderList;
        }
        return members;
    }


}
