package com.krsm.controller;

import com.krsm.entity.Category;
import com.krsm.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", service.getAllCategories());
        return "category_index";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new Category());
        return "category_add";
    }

    @PostMapping("/add")
    public String addCategory(@ModelAttribute Category category) {
        service.saveCategory(category);
        return "redirect:/categories";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        service.getCategoryById(id).ifPresent(cat -> model.addAttribute("category", cat));
        return "category_edit";
    }

    @PostMapping("/edit/{id}")
    public String updateCategory(@PathVariable Long id, @ModelAttribute Category category) {
        category.setId(id);
        service.saveCategory(category);
        return "redirect:/categories";
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        service.deleteCategory(id);
        return "redirect:/categories";
    }
}
