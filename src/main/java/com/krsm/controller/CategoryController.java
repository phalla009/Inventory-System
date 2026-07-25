package com.krsm.controller;

import com.krsm.entity.Category;
import com.krsm.service.CategoryService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
import java.util.Optional;

@Controller
@RequestMapping("/categories")
@Tag(name = "Category", description = "Endpoints for managing product categories")
public class CategoryController {

	private final CategoryService service;

	public CategoryController(CategoryService service) {
		this.service = service;
	}

	/* ================= LIST (HTML PAGE) - HIDDEN ================= */
	@Hidden
	@GetMapping
	public String listCategories(Model model) {
		model.addAttribute("categories", service.getAllCategories());
		return "category/index";
	}

	/* ================= LIST (JSON) ================= */
	@Operation(summary = "List all categories", description = "Returns all categories as JSON")
	@ApiResponses({ 
		@ApiResponse(
			responseCode = "200", 
			description = "Categories returned",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Category.class))
		) 
	})
	@GetMapping("/api")
	@ResponseBody
	public List<Category> listCategoriesJson() {
		return service.getAllCategories();
	}

	/* ================= ADD (HTML FORM) - HIDDEN ================= */
	@Hidden 
	@GetMapping("/add")
	public String showAddForm(Model model) {
		model.addAttribute("category", new Category());
		return "category/add_category";
	}

	/* ================= ADD (POST ACTION) ================= */
	@Operation(
		summary = "Create a new category",
		requestBody = @RequestBody(
			description = "Category object to create",
			required = true,
			content = @Content(
				mediaType = MediaType.APPLICATION_JSON_VALUE,
				schema = @Schema(implementation = Category.class)
			)
		)
	)
	@PostMapping("/add")
	public String addCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
		service.saveCategory(category);
		redirectAttributes.addFlashAttribute("successMessage", "Category added successfully!");
		return "redirect:/categories";
	}

	/* ================= EDIT (HTML FORM) - HIDDEN ================= */
	@Hidden
	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
		Optional<Category> optionalCategory = service.getCategoryById(id);
		if (optionalCategory.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "⚠️ Category not found!");
			return "redirect:/categories";
		}
		model.addAttribute("category", optionalCategory.get());
		return "category/edit_category";
	}

	/* ================= EDIT (JSON) ================= */
	@Operation(summary = "Get category data for editing", description = "Returns single category details as JSON")
	@ApiResponses({ 
		@ApiResponse(
			responseCode = "200", 
			description = "Category returned",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Category.class))
		),
		@ApiResponse(responseCode = "404", description = "Category not found") 
	})
	@GetMapping("/edit/{id}/api")
	@ResponseBody
	public ResponseEntity<Category> showEditFormJson(
			@Parameter(description = "ID of the category") @PathVariable Long id) {
		return service.getCategoryById(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	/* ================= UPDATE (PUT ACTION) ================= */
	@Operation(
		summary = "Update an existing category",
		requestBody = @RequestBody(
			description = "Updated category object",
			required = true,
			content = @Content(
				mediaType = MediaType.APPLICATION_JSON_VALUE,
				schema = @Schema(implementation = Category.class)
			)
		)
	)
	@PutMapping("/edit/{id}")
	public String updateCategory(@PathVariable Long id, @ModelAttribute Category category, RedirectAttributes redirectAttributes) {
		category.setId(id);
		service.saveCategory(category);
		redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully!");
		return "redirect:/categories";
	}

	/* ================= DELETE (CONFIRM FORM) - HIDDEN ================= */
	@Hidden
	@GetMapping("/delete/{id}")
	public String showDeleteForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
		Optional<Category> optionalCategory = service.getCategoryById(id);
		if (optionalCategory.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "⚠️ Category not found!");
			return "redirect:/categories";
		}
		model.addAttribute("category", optionalCategory.get());
		return "category/delete_category";
	}

	/* ================= DELETE (CONFIRM FORM - JSON) ================= */
	@Operation(summary = "Get category data before delete", description = "Returns category details in JSON before confirmation")
	@ApiResponses({ 
		@ApiResponse(
			responseCode = "200", 
			description = "Category returned",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Category.class))
		),
		@ApiResponse(responseCode = "404", description = "Category not found") 
	})
	@GetMapping("/delete/{id}/api")
	@ResponseBody
	public ResponseEntity<Category> showDeleteFormJson(
			@Parameter(description = "ID of the category") @PathVariable Long id) {
		return service.getCategoryById(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	/* ================= DELETE (SINGLE) ================= */
	@Operation(summary = "Delete a single category")
	@DeleteMapping("/delete/{id}")
	public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		Optional<Category> optionalCategory = service.getCategoryById(id);

		if (optionalCategory.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Category not found!");
			return "redirect:/categories";
		}

		boolean hasProducts = service.hasProducts(id);
		if (hasProducts) {
			redirectAttributes.addFlashAttribute("errorMessage", "❌ Cannot delete category! It is used by existing products.");
			return "redirect:/categories";
		}

		service.deleteCategory(id);
		redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully!");
		return "redirect:/categories";
	}

	/* ================= DELETE (MULTIPLE) ================= */
	@Operation(summary = "Delete multiple categories")
	@DeleteMapping("/delete-multiple")
	public String deleteMultipleCategories(
			@RequestParam(value = "categoryIds", required = false) List<Long> ids,
			RedirectAttributes redirectAttributes) {

		if (ids == null || ids.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "⚠️ No categories were selected.");
			return "redirect:/categories";
		}

		List<Long> deletableIds = new ArrayList<>();
		int restrictedCount = 0;

		for (Long id : ids) {
			if (service.hasProducts(id)) {
				restrictedCount++;
			} else {
				deletableIds.add(id);
			}
		}

		if (!deletableIds.isEmpty()) {
			service.deleteAllById(deletableIds);
		}

		if (restrictedCount > 0 && !deletableIds.isEmpty()) {
			redirectAttributes.addFlashAttribute("successMessage", "Successfully deleted " + deletableIds.size() + " category/categories.");
			redirectAttributes.addFlashAttribute("errorMessage", restrictedCount + " category/categories could not be deleted because they are assigned to active products.");
		} else if (restrictedCount > 0) {
			redirectAttributes.addFlashAttribute("errorMessage", "None of the selected categories could be deleted! They are assigned to active products.");
		} else {
			redirectAttributes.addFlashAttribute("successMessage", "Selected categories deleted successfully!");
		}

		return "redirect:/categories";
	}
}