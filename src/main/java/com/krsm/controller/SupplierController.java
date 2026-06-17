package com.krsm.controller;

import com.krsm.entity.Supplier;
import com.krsm.repository.SupplierRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/suppliers")
public class SupplierController {

	private final SupplierRepository supplierRepository;

	public SupplierController(SupplierRepository supplierRepository) {
		this.supplierRepository = supplierRepository;
	}

	// ✅ List all suppliers
	@GetMapping
	public String listSuppliers(Model model) {
		model.addAttribute("suppliers", supplierRepository.findAll());
		return "suppliers/index";
	}

	// ✅ Show Add Supplier form
	@GetMapping("/add")
	public String showAddForm(Model model) {
		model.addAttribute("supplier", new Supplier());
		return "suppliers/add_supplier";
	}

	// ✅ Handle Add Supplier form submission
	@PostMapping("/add")
	public String addSupplier(@ModelAttribute Supplier supplier, RedirectAttributes redirectAttributes) {

		// Check duplicate email
		if (supplierRepository.existsByEmail(supplier.getEmail())) {
			redirectAttributes.addFlashAttribute("errorMessage", "Email already exists!");
			return "redirect:/suppliers"; // go back to form
		}

		// Save if not duplicate
		supplierRepository.save(supplier);
		redirectAttributes.addFlashAttribute("successMessage", "Supplier added successfully!");

		return "redirect:/suppliers";
	}

	// ✅ Show Edit Supplier form
	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable Long id, Model model) {
		supplierRepository.findById(id).ifPresent(supplier -> model.addAttribute("supplier", supplier));
		return "suppliers/edit_supplier";
	}

	// ✅ Handle Edit Supplier form submission

	@PostMapping("/edit/{id}")
	public String updateSupplier(@PathVariable Long id, @ModelAttribute Supplier supplier,
			RedirectAttributes redirectAttributes) {

		// Check duplicate email for other suppliers
		if (supplierRepository.existsByEmailAndIdNot(supplier.getEmail(), id)) {
			redirectAttributes.addFlashAttribute("errorMessage", "Email already exists!");
			return "redirect:/suppliers"; // go back to edit form
		}

		supplier.setId(id);
		supplierRepository.save(supplier);
		redirectAttributes.addFlashAttribute("successMessage", "Supplier updated successfully!");
		return "redirect:/suppliers";
	}

	// ✅ Delete a supplier
	@PostMapping("/delete/{id}")
	public String deleteSupplier(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			supplierRepository.deleteById(id);
			redirectAttributes.addFlashAttribute("successMessage", "Supplier deleted successfully!");
		} catch (DataIntegrityViolationException e) {
			// If supplier is linked to products
			redirectAttributes.addFlashAttribute("errorMessage",
					"Cannot delete supplier. There are products linked to this supplier.");
		}
		return "redirect:/suppliers";
	}
}
