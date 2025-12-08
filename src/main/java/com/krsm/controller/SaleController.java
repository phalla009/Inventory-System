package com.krsm.controller;

import com.krsm.entity.Product;
import com.krsm.entity.SaleDetail;
import com.krsm.entity.Sales;
import com.krsm.repository.ProductRepository;
import com.krsm.repository.SaleDetailRepository;
import com.krsm.repository.SaleRepository;
import com.krsm.service.SaleService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/sales")
public class SaleController {

	private final SaleRepository saleRepository;
	private final ProductRepository productRepository;

	public SaleController(SaleRepository saleRepository, ProductRepository productRepository) {
		this.saleRepository = saleRepository;
		this.productRepository = productRepository;
	}

	// ✅ 1. List all sales
	@GetMapping
	public String listSales(Model model) {
		List<Sales> salesList = saleRepository.findAll();
		model.addAttribute("salesList", salesList);
		return "sales/index";
	}

	// ✅ 2. Show form to add new sale
	@GetMapping("/add")
	public String showAddForm(Model model) {
		model.addAttribute("sale", new Sales());
		model.addAttribute("productsList", productRepository.findAll());
		return "sales/add_sale";
	}

	// ✅ 3. Save new sale with stock update
	@PostMapping("/save")
	public String saveSale(@ModelAttribute("sale") Sales sale, @RequestParam("productId") Long productId,
			RedirectAttributes redirectAttributes) {

		// Fetch the full product from DB
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new IllegalArgumentException("❌ Invalid Product ID"));

		// Check stock
		int newQty = product.getQuantity() - sale.getQuantity();
		if (newQty < 0) {
			redirectAttributes.addFlashAttribute("errorMessage", "❌ Not enough stock!");
			return "redirect:/sales/add";
		}

		// Update stock
		product.setQuantity(newQty);
		productRepository.save(product);

		// Set sale details
		sale.setProduct(product);
		sale.setCreated_at(LocalDateTime.now());
		sale.setSubtotal(sale.getQuantity() * sale.getPrice());
		saleRepository.save(sale);

		redirectAttributes.addFlashAttribute("successMessage", "✅ Sale added successfully!");
		return "redirect:/sales";
	}

	// ✅ 4. Show form to edit sale
	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable("id") Long id, Model model) {
		Sales sale = saleRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("❌ Invalid Sale ID: " + id));
		model.addAttribute("sale", sale);
		model.addAttribute("productsList", productRepository.findAll());
		return "sales/edit_sale";
	}

	// ✅ 5. Update sale with stock adjustment
	@PostMapping("/update/{id}")
	public String updateSale(@PathVariable("id") Long id, @ModelAttribute("sale") Sales saleDetails,
			RedirectAttributes redirectAttributes) {

		Sales sale = saleRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("❌ Invalid Sale ID: " + id));

		// Fetch full product from DB
		Product product = productRepository.findById(saleDetails.getProduct().getId())
				.orElseThrow(() -> new IllegalArgumentException("❌ Invalid Product ID"));

		// Calculate stock difference
		int qtyDiff = saleDetails.getQuantity() - sale.getQuantity();
		int newQty = product.getQuantity() - qtyDiff;
		if (newQty < 0) {
			redirectAttributes.addFlashAttribute("errorMessage", "❌ Not enough stock!");
			return "redirect:/sales/edit/" + id;
		}

		// Update stock
		product.setQuantity(newQty);
		productRepository.save(product);

		// Update sale
		sale.setSale_date(saleDetails.getSale_date());
		sale.setCustomer_name(saleDetails.getCustomer_name());
		sale.setProduct(product);
		sale.setQuantity(saleDetails.getQuantity());
		sale.setPrice(saleDetails.getPrice());
		sale.setSubtotal(saleDetails.getQuantity() * saleDetails.getPrice());
		saleRepository.save(sale);

		redirectAttributes.addFlashAttribute("successMessage", "✅ Sale updated successfully!");
		return "redirect:/sales";
	}

	// ✅ 6. Delete sale and restore stock
	@PostMapping("/delete/{id}")
	public String deleteSale(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
		Sales sale = saleRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("❌ Invalid Sale ID: " + id));

		// Restore stock
		Product product = sale.getProduct();
		product.setQuantity(product.getQuantity() + sale.getQuantity());
		productRepository.save(product);

		saleRepository.delete(sale);
		redirectAttributes.addFlashAttribute("successMessage", "🗑️ Sale deleted successfully!");
		return "redirect:/sales";
	}

	// 7. Show printable receipt for a sale
	/*
	 * @GetMapping("/print/{id}") public String printSale(@PathVariable("id") Long
	 * id, Model model) { Sales sale = saleRepository.findById(id) .orElseThrow(()
	 * -> new IllegalArgumentException("❌ Invalid Sale ID: " + id));
	 * model.addAttribute("sale", sale); return "sales/receipt"; }
	 */


	@GetMapping("/print/batch")
	public String printBatch(@RequestParam("ids") String ids, Model model) {
		List<Long> saleIds = Arrays.stream(ids.split(",")).map(Long::parseLong).toList();

		List<Sales> selectedSales = saleRepository.findAllById(saleIds);

		// For simplicity, assume each sale has a list of SaleDetails (product, qty,
		// price)
		model.addAttribute("selectedSales", selectedSales);
		return "sales/batch_print";
	}

}
