package mk.comm;

import mk.comm.Community.Community;
import mk.comm.Member.Member;
import mk.comm.Role.Role;
import mk.comm.Service.CommunityService;
import mk.comm.Service.MemberService;
import mk.comm.Service.RoleService;
import mk.comm.Service.UserService;
import mk.comm.User.CurrentUser;
import mk.comm.User.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashSet;
import java.util.Set;

@Controller
public class Start {

    @Autowired
    UserService userService;
    @Autowired
    RoleService roleService;
    @Autowired
    CommunityService communityService;
    @Autowired
    MemberService memberService;

    @GetMapping("/")
    public String start(@AuthenticationPrincipal CurrentUser customUser, Model model) {

        // START of usual code
        if (customUser != null) {
            User entityUser = customUser.getUser();
            boolean isAdmin = false;
            boolean isUser = false;
            boolean isOwner = false;
            for (Role role : entityUser.getRoles()) {
                if (role.getName().equals("ROLE_OWNER")) {
                    isOwner = true;
                }
                if (role.getName().equals("ROLE_ADMIN")) {
                    isAdmin = true;
                }
                if (role.getName().equals("ROLE_USER")) {
                    isUser = true;
                }

            }


            if (isOwner) return "redirect:/owner/"; // owner can admin admins & all users
            if (isAdmin) return "redirect:/admin/"; // admin - admin own users in own community
            if (isUser) return "redirect:/user/";   //  user - belongs to community
        }
        // END of usual code
        // START od firts time code
        // change in application.properties database option from update to 'create'.
        // after executing first time code,  change back to 'update'
        /*Role role0 = new Role();
        Role role1 = new Role();
        Role role2 = new Role();
        role0.setName("ROLE_OWNER");
        role1.setName("ROLE_ADMIN");
        role2.setName("ROLE_USER");
        roleService.save(role0);
        roleService.save(role1);
        roleService.save(role2);
        Set<Role> roles = new HashSet<>();
        roles.add(role0);
        roles.add(role1);
        roles.add(role2);
        User user = new User();
        Community community = new Community();
        user.setUsername("a@a.pl"); //<- here your email that will be also a login
        user.setName("Michał");
        user.setSurname("Kopiczyński");
        user.setPassword("123");
        user.setPasswordCheck("123");
        user.setEnabled(1);
        user.setRoles(roles);
        userService.saveUser(user);
        community.setUserId(user.getId());
        community.setName(user.getUsername());
        communityService.save(community);
        Member member = new Member();
        member.setCommunityId(community.getId());
        member.setEmail(user.getUsername());
        member.setAttendance(1);
        member.setName(user.getName());
        member.setSurname(user.getSurname());
        member.setSex('M');
        memberService.save(member);*/
        // END od first time code

        return "landing";
    }
}


