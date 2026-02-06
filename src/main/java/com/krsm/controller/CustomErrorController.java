package com.krsm.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        // អ្នកអាច log error details
        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        System.out.println("Error Status: " + status);
        return "error"; 
    }
}
