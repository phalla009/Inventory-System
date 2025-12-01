package com.krsm.controller;

import com.krsm.entity.Users;
import com.krsm.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	public String listUsers(Model model) {
		model.addAttribute("users", userService.getAllUsers());
		return "user/index";
	}

	@GetMapping("/add-form")
	public String loadAddUserForm(Model model) {
		model.addAttribute("user", new Users());
		return "user/add_user :: form";
	}

	@PostMapping("/add")
	public String addUser(@ModelAttribute Users user, RedirectAttributes redirect) {
		try {
			userService.saveUser(user);
			redirect.addFlashAttribute("successMessage", "User added successfully!");
		} catch (Exception e) {
			redirect.addFlashAttribute("errorMessage", "Failed to add user.");
		}
		return "redirect:/user";
	}

	@GetMapping("/edit-form/{id}")
	public String loadEditUserForm(@PathVariable Long id, Model model) {
		model.addAttribute("user", userService.getUserById(id));
		return "user/edit_user :: form";
	}

	@PostMapping("/edit/{id}")
	public String editUser(@PathVariable Long id, @ModelAttribute Users user, RedirectAttributes redirect) {
		try {
			user.setId(id);
			userService.saveUser(user);
			redirect.addFlashAttribute("successMessage", "User updated successfully!");
		} catch (Exception e) {
			redirect.addFlashAttribute("errorMessage", "Failed to update user.");
		}
		return "redirect:/user";
	}

	@PostMapping("/delete/{id}")
	public String deleteUser(@PathVariable Long id, RedirectAttributes redirect) {
		try {
			userService.deleteUser(id);
			redirect.addFlashAttribute("successMessage", "User deleted successfully!");
		} catch (Exception e) {
			redirect.addFlashAttribute("errorMessage", "Failed to delete user.");
		}
		return "redirect:/user";
	}

}
