package com.krsm.controller;

import com.krsm.entity.Users;
import com.krsm.service.UserService;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

	private final UserService userService;

	public LoginController(UserService userService) {
		this.userService = userService;
	}

	// ================================
	// SHOW LOGIN PAGE (GET)
	// ================================
	@GetMapping({ "/", "/login" })
	public String loginForm(Model model, @RequestParam(value = "error", required = false) String error,
			HttpSession session) {

		// If already logged in → Go to dashboard
		if (session.getAttribute("userRole") != null) {
			return "redirect:/dashboard";
		}

		model.addAttribute("roles", userService.getAllRoles());

		if (error != null) {
			model.addAttribute("errorMessage", "Invalid username, password, or role!");
		}

		return "login/index";
	}

	// ================================
	// PROCESS LOGIN FORM (POST)
	// ================================
	@PostMapping("/login")
	public String login(@RequestParam String username, @RequestParam String password, @RequestParam String role,
			HttpSession session, Model model) {

		Users user = userService.authenticate(username, password, role);

		if (user != null) {
			session.setAttribute("userRole", user.getRole());
			session.setAttribute("username", user.getUsername());
			return "redirect:/dashboard";
		} else {
			model.addAttribute("roles", userService.getAllRoles());
			model.addAttribute("errorMessage", "Invalid username, password, or role!");
			return "login/index";
		}
	}

	// ================================
	// DASHBOARD (PROTECTED)
	// ================================
	@GetMapping("/dashboard")
	public String dashboard(HttpSession session) {
		if (session.getAttribute("userRole") == null) {
			return "redirect:/login";
		}
		return "dashboard";
	}

	// ================================
	// LOGOUT
	// ================================
	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/login";
	}
}
