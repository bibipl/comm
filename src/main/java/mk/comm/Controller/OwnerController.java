package mk.comm.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping ("/owner")
public class OwnerController {

    @GetMapping("/")
        public String ownerStart() {
        return "/owner/start";
        }

}
