package com.sist.baemin.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UserViewController {

    @GetMapping("/api/login")
    public String loginPage() {
        return "html/login";
    }

    @GetMapping("/")
    public String rootRedirectToLogin() {
        return "redirect:/api/login";
    }
}
