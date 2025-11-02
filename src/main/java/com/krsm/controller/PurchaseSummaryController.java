package com.krsm.controller;

import com.krsm.entity.Purchases;
import com.krsm.repository.PurchasesRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/purchase-summary")
public class PurchaseSummaryController {

	private final PurchasesRepository purchaseRepository;

	public PurchaseSummaryController(PurchasesRepository purchaseRepository) {
		this.purchaseRepository = purchaseRepository;
	}

	@GetMapping
	public String showSummary(Model model) {
		// Get all purchases
		List<Purchases> purchaseList = purchaseRepository.findAll();

		// Calculate totals
		double totalPrice = purchaseList.stream()
				.mapToDouble(p -> p.getTotal_amount() != null ? p.getTotal_amount() : 0).sum();
		int totalQty = purchaseList.stream().mapToInt(Purchases::getQuantity).sum();
		long totalSupplier = purchaseList.stream().map(p -> p.getSupplier().getName()).distinct().count();

		// Add attributes to model
		model.addAttribute("purchaseList", purchaseList);
		model.addAttribute("totalPrice", totalPrice);
		model.addAttribute("totalQty", totalQty);
		model.addAttribute("totalSupplier", totalSupplier);

		return "purchase_summary/index"; 
	}
}
