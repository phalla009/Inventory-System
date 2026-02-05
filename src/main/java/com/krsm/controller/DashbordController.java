package com.krsm.controller;

import com.krsm.entity.Product;
import com.krsm.entity.Sales;
import com.krsm.repository.ProductRepository;
import com.krsm.repository.SaleRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class DashbordController {

	private final ProductRepository productRepository;
	private final SaleRepository saleRepository;

	public DashbordController(ProductRepository productRepository, SaleRepository saleRepository) {
		this.productRepository = productRepository;
		this.saleRepository = saleRepository;
	}

	@GetMapping("/dashboard")
	public String dashboard(Model model) {

	    long totalProducts = productRepository.count();
	    int totalQuantity = productRepository.findAll().stream().mapToInt(Product::getQuantity).sum();
	    LocalDate today = LocalDate.now();

	    double totalDaily = saleRepository.findAll().stream()
	            .filter(s -> s.getSale_date().toLocalDate().isEqual(today))
	            .mapToDouble(Sales::getSubtotal).sum();

	    YearMonth thisMonth = YearMonth.now();
	    double totalMonthly = saleRepository.findAll().stream()
	            .filter(s -> YearMonth.from(s.getSale_date().toLocalDate()).equals(thisMonth))
	            .mapToDouble(Sales::getSubtotal).sum();

	    double grandTotal = saleRepository.findAll().stream().mapToDouble(Sales::getSubtotal).sum();

	    model.addAttribute("totalProducts", totalProducts);
	    model.addAttribute("totalQuantity", totalQuantity);
	    model.addAttribute("totalDaily", totalDaily);
	    model.addAttribute("totalMonthly", totalMonthly);
	    model.addAttribute("grandTotal", grandTotal);

	    // Stock value by category
	    Map<String, Double> stockByCategory = productRepository.findAll().stream()
	            .collect(Collectors.groupingBy(
	                    p -> p.getCategory().getName(),  // ✅ Use category name
	                    Collectors.summingDouble(p -> p.getQuantity() * p.getPrice())
	            ));

	    model.addAttribute("categoryLabels", stockByCategory.keySet().stream().toList());
	    model.addAttribute("categoryData", stockByCategory.values().stream().toList());
	    
	 // Sales per Supplier
	    Map<String, Double> salesBySupplier = saleRepository.findAll().stream()
	            .collect(Collectors.groupingBy(
	                    s -> s.getProduct().getSupplier().getName(), // Use supplier name
	                    Collectors.summingDouble(Sales::getSubtotal) // Total sales per supplier
	            ));

	    List<String> supplierLabels = salesBySupplier.keySet().stream().toList();
	    List<Double> supplierData = salesBySupplier.values().stream().toList();

	    model.addAttribute("supplierLabels", supplierLabels);
	    model.addAttribute("supplierData", supplierData);


	    return "dashboard";
	}

}
