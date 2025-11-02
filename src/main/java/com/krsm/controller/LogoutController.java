package com.krsm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;


@Controller
public class LogoutController {

	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate(); // clear session
		return "redirect:/login?logout"; // redirect to login with optional message
	}
}
