package com.krsm.controller;

import com.krsm.entity.Purchases;
import com.krsm.repository.PurchasesRepository;
import com.krsm.repository.SupplierRepository;
import com.krsm.repository.ProductRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/purchases")
public class PurchaseController {

	private final PurchasesRepository purchaseRepository;
	private final SupplierRepository supplierRepository;
	private final ProductRepository productRepository;

	public PurchaseController(PurchasesRepository purchaseRepository, SupplierRepository supplierRepository,
			ProductRepository productRepository) {
		this.purchaseRepository = purchaseRepository;
		this.supplierRepository = supplierRepository;
		this.productRepository = productRepository;
	}

	// List all purchases
	@GetMapping
	public String listPurchases(Model model) {
		model.addAttribute("purchases", purchaseRepository.findAll());
		return "purchases/index";
	}

	// Add purchase form
	@GetMapping("/add")
	public String showAddForm(Model model) {
		model.addAttribute("purchase", new Purchases());
		model.addAttribute("suppliers", supplierRepository.findAll());
		model.addAttribute("products", productRepository.findAll());
		return "purchases/add_purchase";
	}

	// Save purchase
	@PostMapping("/add")
	public String addPurchase(@ModelAttribute Purchases purchase, RedirectAttributes redirectAttributes) {
		// Calculate total amount automatically if needed
		if (purchase.getQuantity() > 0 && purchase.getPrice() > 0) {
			purchase.setTotal_amount(purchase.getQuantity() * purchase.getPrice());
		}

		purchaseRepository.save(purchase);
		redirectAttributes.addFlashAttribute("successMessage", "Purchase added successfully!");
		return "redirect:/purchases";
	}

	// Edit purchase form
	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable Long id, Model model) {
		purchaseRepository.findById(id).ifPresent(p -> model.addAttribute("purchase", p));
		model.addAttribute("suppliers", supplierRepository.findAll());
		model.addAttribute("products", productRepository.findAll());
		return "purchases/edit_purchase";
	}

	// Update purchase
	@PostMapping("/edit/{id}")
	public String updatePurchase(@PathVariable Long id, @ModelAttribute Purchases purchase,
			RedirectAttributes redirectAttributes) {
		purchaseRepository.findById(id).ifPresent(existingPurchase -> {
			existingPurchase.setSupplier(purchase.getSupplier());
			existingPurchase.setProduct(purchase.getProduct());
			existingPurchase.setQuantity(purchase.getQuantity());
			existingPurchase.setPrice(purchase.getPrice());
			existingPurchase.setTotal_amount(purchase.getQuantity() * purchase.getPrice());
			existingPurchase.setPurchase_date(purchase.getPurchase_date());
			purchaseRepository.save(existingPurchase);
		});
		redirectAttributes.addFlashAttribute("successMessage", "Purchase updated successfully!");
		return "redirect:/purchases";
	}

	// Delete purchase
	@PostMapping("/delete/{id}")
	public String deletePurchase(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			purchaseRepository.deleteById(id);
			redirectAttributes.addFlashAttribute("successMessage", "Purchase deleted successfully!");
		} catch (DataIntegrityViolationException e) {
			redirectAttributes.addFlashAttribute("errorMessage",
					"Cannot delete this purchase: it is linked to other records.");
		}
		return "redirect:/purchases";
	}
}
