package com.krsm.controller;

import com.krsm.entity.Users;
import com.krsm.service.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/user")
@Tag(name = "User", description = "Endpoints for managing system users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	/* ================= LIST (HTML PAGE) - HIDDEN ================= */
	@Hidden // លាក់ HTML Page
	@GetMapping
	public String listUsers(Model model) {
		model.addAttribute("users", userService.getAllUsers());
		return "user/index";
	}

	/* ================= LIST (JSON) ================= */
	@Operation(summary = "List all users", description = "Returns all users as a JSON array")
	@ApiResponses({ 
		@ApiResponse(
			responseCode = "200", 
			description = "Users returned",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Users.class))
		) 
	})
	@GetMapping("/api")
	@ResponseBody
	public List<Users> listUsersJson() {
		return userService.getAllUsers();
	}

	/* ================= ADD (HTML FRAGMENT / FORM) - HIDDEN ================= */
	@Hidden // លាក់ HTML Form Fragment
	@GetMapping("/add-form")
	public String loadAddUserForm(Model model) {
		model.addAttribute("user", new Users());
		return "user/add_user :: form";
	}

	/* ================= ADD (POST ACTION) ================= */
	@Operation(
		summary = "Create a new user",
		requestBody = @RequestBody(
			description = "JSON payload for creating a user",
			required = true,
			content = @Content(
				mediaType = MediaType.APPLICATION_JSON_VALUE,
				schema = @Schema(implementation = Users.class),
				examples = @ExampleObject(
					name = "New User Example",
					value = "{\n  \"username\": \"john_doe\",\n  \"password\": \"secret123\",\n  \"role\": \"User\"\n}"
				)
			)
		)
	)
	@ApiResponses({ @ApiResponse(responseCode = "302", description = "Redirects to /user after creation") })
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

	/* ================= EDIT (HTML FRAGMENT / FORM) - HIDDEN ================= */
	@Hidden // លាក់ HTML Form Fragment
	@GetMapping("/edit-form/{id}")
	public String loadEditUserForm(@PathVariable Long id, Model model) {
		model.addAttribute("user", userService.getUserById(id));
		return "user/edit_user :: form";
	}

	/* ================= EDIT (JSON) ================= */
	@Operation(summary = "Get user data for editing", description = "Returns single user details as JSON")
	@ApiResponses({ 
		@ApiResponse(
			responseCode = "200", 
			description = "User returned",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Users.class))
		),
		@ApiResponse(responseCode = "404", description = "User not found") 
	})
	@GetMapping("/edit/{id}/api")
	@ResponseBody
	public ResponseEntity<Users> showEditFormJson(
			@Parameter(description = "ID of the user to edit") @PathVariable Long id) {
		Users user = userService.getUserById(id);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		return ResponseEntity.ok(user);
	}

	/* ================= UPDATE (PUT ACTION) ================= */
	@Operation(
		summary = "Update an existing user",
		requestBody = @RequestBody(
			description = "JSON payload for updating a user",
			required = true,
			content = @Content(
				mediaType = MediaType.APPLICATION_JSON_VALUE,
				schema = @Schema(implementation = Users.class),
				examples = @ExampleObject(
					name = "Update User Example",
					value = "{\n  \"username\": \"john_doe_updated\",\n  \"password\": \"newsecret123\",\n  \"role\": \"Admin\"\n}"
				)
			)
		)
	)
	@PutMapping("/edit/{id}")
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

	/* ================= DELETE (CONFIRM - JSON) ================= */
	@Operation(summary = "Get user data for delete confirmation", description = "Returns user details in JSON before confirmation")
	@ApiResponses({ 
		@ApiResponse(
			responseCode = "200", 
			description = "User returned",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Users.class))
		),
		@ApiResponse(responseCode = "404", description = "User not found") 
	})
	@GetMapping("/delete/{id}/api")
	@ResponseBody
	public ResponseEntity<Users> showDeleteFormJson(
			@Parameter(description = "ID of the user to delete") @PathVariable Long id) {
		Users user = userService.getUserById(id);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		return ResponseEntity.ok(user);
	}

	/* ================= DELETE (SINGLE) ================= */
	@Operation(summary = "Delete a single user")
	@ApiResponses({
			@ApiResponse(responseCode = "302", description = "Redirects to /user with a success or error flash message") })
	@DeleteMapping("/delete/{id}")
	public String deleteUser(@Parameter(description = "ID of the user to delete") @PathVariable Long id,
			RedirectAttributes redirect) {
		try {
			userService.deleteUser(id);
			redirect.addFlashAttribute("successMessage", "User deleted successfully!");
		} catch (Exception e) {
			redirect.addFlashAttribute("errorMessage",
					"Failed to delete user because it is linked to active transactions or data.");
		}
		return "redirect:/user";
	}

	/* ================= DELETE (MULTIPLE) ================= */
	@Operation(summary = "Delete multiple users", description = "Users linked to system activities are skipped and reported separately")
	@ApiResponses({
			@ApiResponse(responseCode = "302", description = "Redirects to /user with a summary flash message") })
	@DeleteMapping("/delete-multiple")
	public String deleteMultipleUsers(
			@Parameter(description = "IDs of the users to delete") @RequestParam(value = "userIds", required = false) List<Long> ids,
			RedirectAttributes redirect) {

		if (ids == null || ids.isEmpty()) {
			redirect.addFlashAttribute("errorMessage", "⚠️ No users were selected.");
			return "redirect:/user";
		}

		List<Long> deletableIds = new ArrayList<>();
		int restrictedCount = 0;

		for (Long id : ids) {
			try {
				userService.deleteUser(id);
				deletableIds.add(id);
			} catch (Exception e) {
				restrictedCount++;
			}
		}

		if (restrictedCount > 0 && !deletableIds.isEmpty()) {
			redirect.addFlashAttribute("successMessage", "Successfully deleted " + deletableIds.size() + " user(s).");
			redirect.addFlashAttribute("errorMessage",
					restrictedCount + " user(s) could not be deleted because they are linked to active records.");
		} else if (restrictedCount > 0) {
			redirect.addFlashAttribute("errorMessage",
					"None of the selected users could be deleted! They are linked to active records.");
		} else {
			redirect.addFlashAttribute("successMessage", "Selected users deleted successfully!");
		}

		return "redirect:/user";
	}
}