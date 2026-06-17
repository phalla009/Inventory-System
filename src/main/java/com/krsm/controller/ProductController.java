package com.krsm.controller;

import com.krsm.entity.Category;
import com.krsm.entity.Product;
import com.krsm.entity.Supplier;
import com.krsm.repository.CategoryRepository;
import com.krsm.repository.ProductRepository;
import com.krsm.repository.SupplierRepository;
import com.krsm.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/products")
public class ProductController {

	private final ProductRepository productRepository;
	private final ProductService productService;
	private final CategoryRepository categoryRepository;
	private final SupplierRepository supplierRepository;

	public ProductController(ProductService productService, CategoryRepository categoryRepository,
			SupplierRepository supplierRepository, ProductRepository productRepository) {
		this.productService = productService;
		this.categoryRepository = categoryRepository;
		this.supplierRepository = supplierRepository;
		this.productRepository = productRepository;
	}

	// ✅ List all products
	@GetMapping
	public String listProducts(Model model) {
		model.addAttribute("products", productService.getAllProducts());
		return "products/index";
	}

	// ✅ Show Add Product Form
	@GetMapping("/add")
	public String showAddForm(Model model) {
		model.addAttribute("product", new Product());
		model.addAttribute("categories", categoryRepository.findAll());
		model.addAttribute("suppliers", supplierRepository.findAll());
		return "products/add_product";
	}

	// ✅ Handle Save Product
	@PostMapping("/add")
	public String addProduct(@ModelAttribute Product product, @RequestParam("category_id") Long categoryId,
			@RequestParam("supplier_id") Long supplierId, RedirectAttributes redirectAttributes) {

		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + categoryId));
		Supplier supplier = supplierRepository.findById(supplierId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid supplier ID: " + supplierId));

		product.setCategory(category);
		product.setSupplier(supplier);
		product.setCreate_at(LocalDateTime.now());

		// ✅ Respect user-selected status
		String selectedStatus = product.getStatus();
		if (product.getQuantity() == 0 && !"Inactive".equals(selectedStatus)) {
			product.setStatus("Out of Stock");
		} else if (product.getQuantity() > 0 && "Out of Stock".equals(selectedStatus)) {
			product.setStatus("Active");
		} else {
			product.setStatus(selectedStatus);
		}

		productService.saveProduct(product);
		redirectAttributes.addFlashAttribute("successMessage", "✅ Product saved successfully!");
		return "redirect:/products";
	}

	// ✅ Show Edit Product Form
	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
		Optional<Product> optionalProduct = productService.getProductById(id);
		if (optionalProduct.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "⚠️ Product not found!");
			return "redirect:/products";
		}

		model.addAttribute("product", optionalProduct.get());
		model.addAttribute("categories", categoryRepository.findAll());
		model.addAttribute("suppliers", supplierRepository.findAll());
		return "products/edit_product";
	}

	// ✅ Handle Update Product
	@PostMapping("/edit/{id}")
	public String updateProduct(@PathVariable Long id, @ModelAttribute Product product,
			@RequestParam("category_id") Long categoryId, @RequestParam("supplier_id") Long supplierId,
			RedirectAttributes redirectAttributes) {

		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + categoryId));
		Supplier supplier = supplierRepository.findById(supplierId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid supplier ID: " + supplierId));

		product.setId(id);
		product.setCategory(category);
		product.setSupplier(supplier);

		// ✅ Respect user-selected status on edit
		String selectedStatus = product.getStatus();
		if (product.getQuantity() == 0 && !"Inactive".equals(selectedStatus)) {
			product.setStatus("Out of Stock");
		} else if (product.getQuantity() > 0 && "Out of Stock".equals(selectedStatus)) {
			product.setStatus("Active");
		} else {
			product.setStatus(selectedStatus);
		}

		productService.saveProduct(product);
		redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully!");
		return "redirect:/products";
	}

	// ✅ Handle Delete Product with foreign key check
	@PostMapping("/delete/{id}")
	public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		Optional<Product> optionalProduct = productRepository.findById(id);

		if (optionalProduct.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Product not found!");
			return "redirect:/products";
		}

		Product product = optionalProduct.get();

		boolean hasSales = !productService.hasSales(product.getId());
		boolean hasPurchases = !productService.hasPurchases(product.getId());

		if (hasSales || hasPurchases) {
			redirectAttributes.addFlashAttribute("errorMessage",
					"Cannot delete product! It is used in existing sales or purchases.");
			return "redirect:/products";
		}

		productService.deleteProduct(id);
		redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully!");
		return "redirect:/products";
	}

}
