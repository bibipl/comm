package mk.comm.Controller;

import mk.comm.Role.Role;
import mk.comm.Service.EmailSender;
import mk.comm.Service.RoleService;
import mk.comm.Service.UserService;
import mk.comm.Service.VerificationTokenService;
import mk.comm.User.User;
import mk.comm.verificationToken.VerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Controller
public class RegisterController {
    @Autowired
    UserService userService;
    @Autowired
    RoleService roleService;
    @Autowired
    VerificationTokenService verificationTokenService;
    @Autowired
    EmailSender emailSender;
    @Autowired
    TemplateEngine templateEngine;

    @GetMapping("/register")
    public String registerForm (Model model) {
        User user = new User();
        model.addAttribute("user", user)  ;
        return "/register/register";
    }
    @PostMapping("/register")
    public String registerAction (@ModelAttribute @Valid User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("user", user);
            return "/register/register";
        }
        else if (!user.getPassword().equals(user.getPasswordCheck())) {
            return"/register/register";
        }
        User userCheckUsername = userService.findByUsername(user.getUsername());

        if (userCheckUsername == null) {
            Role role=roleService.findByName("ROLE_ADMIN");
            Role role1=roleService.findByName("ROLE_USER");
            Set<Role> allRoles = new HashSet<>();
            allRoles.add(role);
            allRoles.add(role1);
            user.setRoles(allRoles);
            user.setEnabled(0);
            userService.saveUser(user); // we have to save to get id.
            String token = UUID.randomUUID().toString();
            userService.saveUser(user); // we have to save to get id.
            VerificationToken verToken = new VerificationToken();
            verToken.setUser(user);
            verToken.setToken(token);
            verificationTokenService.save(verToken);
            Context context = new Context();
            context.setVariable("header", "Rejestracja w serwisie Zarządzaj wspólnotą");
            context.setVariable("title", "Witaj " + user.getUsername() + "!");
            context.setVariable("description", "Dziękujemy za rejestrację w naszym serwisie. Dokoncz swoj proces rejestracji");
            context.setVariable("link","http://localhost:8080/confirm/" + token + "/" + user.getId());
            String body = templateEngine.process("templateRegister", context);
            emailSender.sendEmail(user.getUsername(), "Zarządzaj swoją wspólnotą - witamy !", body);
            return "/login/login";
        }
        return "/login/register";
    }

    @GetMapping ("/confirm/{token}/{id}")
    public String tokenConfirmation (@PathVariable("token")String token, @PathVariable ("id") Long id) {
        // So far do nothing until token entity has been created
        // The plan ids to take token and user id. Find token by userid and compare them.
        User user = userService.findById(id);
        if (user != null) {
            List<VerificationToken> verTokens = verificationTokenService.findAllByUserId(id);
            if (verTokens != null) {
                for (VerificationToken verToken : verTokens) {
                    if (verToken.getToken().equals(token)) {
                        user.setEnabled(1);
                        userService.saveUser(user);
                    }
                    verificationTokenService.delete(verToken);
                }
            }
        }
        return "/login/login";
    }
}
