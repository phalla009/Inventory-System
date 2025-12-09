package com.krsm.controller;

import com.krsm.entity.Product;
import com.krsm.service.ProductService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class DashbordController {

	private final ProductService productService;

	public DashbordController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping({ "/dashboard" })
	public String showDashboard(Model model, HttpSession session) {
		List<Product> products = productService.getAllProducts();
		// Check if user is logged in
        if (session.getAttribute("userRole") == null) {
            return "redirect:/login"; // not logged in → redirect to login
        }
		if (products == null)
			products = new ArrayList<>();

		model.addAttribute("products", products);
		model.addAttribute("totalQuantity", products.stream().mapToInt(Product::getQuantity).sum());
		model.addAttribute("totalValue", products.stream().mapToDouble(p -> p.getPrice() * p.getQuantity()).sum());

		// Get role from session and pass to template
		String userRole = (String) session.getAttribute("userRole");
		model.addAttribute("userRole", userRole);

		return "dashboard";
	}
}
