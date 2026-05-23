package com.syfe.finance_manager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    @GetMapping("/")
    public String index() {
        // Redirect to H2 Console so hitting the root URL doesn't show a 404
        return "redirect:/api/h2-console/";
    }
}
