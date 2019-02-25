package mk.comm.Controller;

import mk.comm.Community.Community;
import mk.comm.Comparators.MemberComparator;
import mk.comm.Member.Member;
import mk.comm.Member.MemberAttr;
import mk.comm.Service.CommunityService;
import mk.comm.Service.EmailSender;
import mk.comm.Service.MemberService;
import mk.comm.User.CurrentUser;
import mk.comm.User.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/communities/member")
public class MemberController {

    @Autowired
    CommunityService communityService;
    @Autowired
    MemberService memberService;
    @Autowired
    TemplateEngine templateEngine;
    @Autowired
    EmailSender emailSender;

    // sets model at proper data ready to go to member Add  - model contains member, community, iam,attendace and sex lists***//
    @GetMapping("/add/{idComm}")
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
                return "/member/addMember";
            }
        }
        String x = getReturnToDetails(member);
        if (x != null) return x;
        return "redirect:/admin/communities";
    }

    @PostMapping("/add")
    public String memberSave (@AuthenticationPrincipal CurrentUser currentUser,
                              @Valid @ModelAttribute Member member, BindingResult result, Model model){

        User user = currentUser.getUser();
        if (member != null && checkUserRightsToModifyMember(user, member)) {
            if (result.hasErrors()) {
                model = addToModelMemberData(model, user, member);
                return "/member/addMember";
            }
            Long idComm = member.getCommunityId();
            boolean admHasRights = false;
            if (idComm > 0) {
                List<Community> communities = communityService.findAllByUserId(user.getId());
                if (communities != null) {
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
                String x = getReturnToDetails(member);
                if (x != null) return x;
            }
        }
        return "redirect:/admin/communities";
    }




    @GetMapping("/edit/{idMemb}")
    public String memberEdit (@AuthenticationPrincipal CurrentUser currentUser,
                              @PathVariable Long idMemb, Model model){
        User user = currentUser.getUser();
        if (idMemb >0) {
            Member member = memberService.findById(idMemb);
            if (member != null && member.getCommunityId() >0 && checkUserRightsToModifyMember(user, member)) {
                model = addToModelMemberData(model, user, member);
                return "/member/addMember";
            }
            String x = getReturnToDetails(member);
            if (x != null) return x;
        }
        return "redirect:/admin/communities";
    }


    @GetMapping("/addMarriage/{idMemb}")
    public String memberMarriage (@AuthenticationPrincipal CurrentUser currentUser, @PathVariable Long idMemb, Model model) {
        User user = currentUser.getUser();
        Member member = null;
        Member member2 = null;
        Community community = null;
        if (idMemb > 0) {
            member = memberService.findById(idMemb);
            if (member != null && member.getCommunityId() > 0 && checkUserRightsToModifyMember(user, member)) {
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
                    MemberComparator memberComparator = new MemberComparator();
                    if (member.getSex() == 'M') {
                        // get all free women
                        marriages = memberService.findAllNotMarriedBySex('K');
                        marriages.sort(memberComparator);
                    } else if (member.getSex() == 'K') {
                        // get all free men
                        marriages = memberService.findAllNotMarriedBySex('M');
                        marriages.sort(memberComparator);
                    } else {
                        if (community.getId() > 0)
                            return "redirect:admin/communities/view/communities/" + community.getId();
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

    @PostMapping("/addMarriage")
    public String memberMarriageSave (@AuthenticationPrincipal CurrentUser currentUser,
                                      @ModelAttribute Member member){
        User user = currentUser.getUser();
        Member member1 = null;
        if (member != null && member.getMarried() >0 && checkUserRightsToModifyMember(user, member)) {
            member1 = memberService.findById(member.getMarried());
            member = memberService.findById(member.getId());
            if (member != null && member1 != null) {
                if (member.getSex() != member1.getSex()) {
                    member.setMarried(member1.getId());
                    member1.setMarried(member.getId());
                    memberService.save(member);
                    memberService.save(member1);
                }
                if (member.getCommunityId() > 0) {
                    return "redirect:/admin/communities/view/" + member.getCommunityId();
                } else {
                    return "redirect:/admin/communities";
                }
            }
        }
        return "redirect:/admin/communities";
    }

    @PostMapping("/tearMarriage")
    public String tearMarriage (@AuthenticationPrincipal CurrentUser currentUser,
                                @ModelAttribute Member member1) {
        User user = currentUser.getUser();
        Community community = null;
        Member member2 = null;
        if (member1 != null && member1.getId() > 0 && member1.getCommunityId() > 0 && checkUserRightsToModifyMember(user, member1)) {
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
                return "redirect:/admin/communities/view/" + member1.getCommunityId();
            }
        }
        return "redirect:/admin/communities";
    }

    @GetMapping("/view/{idMember}")
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
                    return "/member/viewMember";
                }
            }
        }
        return "";
    }
    @GetMapping("/delete/{idMember}")
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
                return "redirect:/admin/communities/view/" + member.getCommunityId();
            }
        }
        return"redirect:/admin/communities";
    }
    @PostMapping("/delete")
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
            return "redirect:/admin/communities/view/" + member1.getCommunityId();
        }
        return "redirect:/admin/communities";
    }




//********************************************************************************************************************
    // *** here we check credentioals - if user can modify member
    // ** check if USER exists is not null and has id >0 alo if MEMBER exists and has community info
    protected boolean checkUserRightsToModifyMember (User user, Member member) {
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

    //**********************************************************************************************************************
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
// **************** returns string adress to detail of the member
    private String getReturnToDetails(@ModelAttribute @Valid Member member) {
        if (member!= null) {
            if (member.getId() > 0) {
                return "redirect:/admin/communities/member/view/" + member.getId();
            }
            if (member.getCommunityId() > 0) {
                return "redirect:/admin/communities/view/" + member.getCommunityId();
            }
        }
        return null;
    }
    // END ***** getReturnToDetails ******
}
