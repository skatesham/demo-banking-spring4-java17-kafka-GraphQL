package com.showcase.banking.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Provides a useful landing page for a backend-only demo. */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/swagger-ui/index.html";
    }
}
