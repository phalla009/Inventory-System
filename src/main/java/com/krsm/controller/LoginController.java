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

	@GetMapping({ "/", "/login" })
	public String loginForm(Model model, @RequestParam(value = "error", required = false) String error) {

		model.addAttribute("roles", userService.getAllRoles());

		if (error != null) {
			model.addAttribute("errorMessage", "Invalid username, password, or role!");
		}

		return "login/index";
	}

	@PostMapping({ "/", "/login" })
	public String login(@RequestParam String username, @RequestParam String password, @RequestParam String role,
			HttpSession session, Model model) {

		Users user = userService.authenticate(username, password, role);

		if (user != null) {
			session.setAttribute("userRole", user.getRole()); // store role in session
			return "redirect:/dashboard";
		} else {
			model.addAttribute("roles", userService.getAllRoles());
			model.addAttribute("errorMessage", "Invalid username, password, or role!");
			return "login/index";
		}
	}

	@RequestMapping("/dashboard")
	public String dashboard() {
		return "dashboard";
	}

	// Protect the dashboard
	@GetMapping("/dashboard")
	public String dashboard(HttpSession session) {
		if (session.getAttribute("userRole") == null) {
			return "redirect:/login"; // not logged in → redirect to login
		}
		return "dashboard";
	}

	// Example for /user page
	@GetMapping("/user")
	public String userPage(HttpSession session) {
		if (session.getAttribute("userRole") == null) {
			return "redirect:/login"; // not logged in → redirect to login
		}
		return "user/index";
	}

	// Example for /products page
	@GetMapping("/products")
	public String productsPage(HttpSession session) {
		if (session.getAttribute("userRole") == null) {
			return "redirect:/login"; // not logged in → redirect to login
		}
		return "products/index";
	}

}
