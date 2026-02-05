package com.krsm.controller;

import com.krsm.entity.Category;
import com.krsm.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    /* ================= LIST ================= */
    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", service.getAllCategories());
        return "category/index"; // your Thymeleaf list page
    }

    /* ================= ADD ================= */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new Category());
        return "category/add_category"; // Thymeleaf add modal/content
    }

    @PostMapping("/add")
    public String addCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        service.saveCategory(category);
        redirectAttributes.addFlashAttribute("successMessage", "✅ Category added successfully!");
        return "redirect:/categories";
    }

    /* ================= EDIT ================= */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Category category = service.getCategoryById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        model.addAttribute("category", category);
        return "category/edit_category"; // Thymeleaf edit modal/content
    }

    @PostMapping("/edit/{id}")
    public String updateCategory(@PathVariable Long id, @ModelAttribute Category category,
                                 RedirectAttributes redirectAttributes) {
        category.setId(id);
        service.saveCategory(category);
        redirectAttributes.addFlashAttribute("successMessage", "✅ Category updated successfully!");
        return "redirect:/categories";
    }

    /* ================= DELETE ================= */
    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Category> optionalCategory = service.getCategoryById(id);

        if (optionalCategory.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "⚠️ Category not found!");
            return "redirect:/categories";
        }

        // Optional: check if category is used in products
        boolean hasProducts = service.hasProducts(id); // implement in service
        if (hasProducts) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "❌ Cannot delete category! It is used by existing products.");
            return "redirect:/categories";
        }

        service.deleteCategory(id);
        redirectAttributes.addFlashAttribute("successMessage", "🗑️ Category deleted successfully!");
        return "redirect:/categories";
    }
}
