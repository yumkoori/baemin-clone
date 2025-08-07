package com.sist.baemin;

import com.sist.baemin.user.domain.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api")
public class MainController {
    @GetMapping("/main")
    public String mainPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("email", userDetails.getUsername());
        } else {
            System.out.println("사용자 이메일 시큐리티에 안담김");
            model.addAttribute("email", null);
        }
        return "html/main";
    }
} 