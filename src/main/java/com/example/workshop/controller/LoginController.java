package com.example.workshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for handling login page requests.
 */
@Controller
public class LoginController {

    /**
     * Displays the login page.
     *
     * @return the login page view
     */
    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";
    }
}

