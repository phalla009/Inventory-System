package com.krsm.controller;

import com.krsm.entity.Category;
import com.krsm.entity.Product;
import com.krsm.entity.StockMovement;
import com.krsm.entity.Supplier;
import com.krsm.repository.CategoryRepository;
import com.krsm.repository.ProductRepository;
import com.krsm.repository.StockMovementRepository;
import com.krsm.repository.SupplierRepository;
import com.krsm.service.ProductService;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/products")
@Tag(name = "Product", description = "Endpoints for managing products")
public class ProductController {

	private final ProductRepository productRepository;
	private final ProductService productService;
	private final CategoryRepository categoryRepository;
	private final SupplierRepository supplierRepository;
	private final StockMovementRepository stockMovementRepository;

	public ProductController(ProductService productService, CategoryRepository categoryRepository,
			SupplierRepository supplierRepository, ProductRepository productRepository,
			StockMovementRepository stockMovementRepository) {
		this.productService = productService;
		this.categoryRepository = categoryRepository;
		this.supplierRepository = supplierRepository;
		this.productRepository = productRepository;
		this.stockMovementRepository = stockMovementRepository;
	}

	/* ================= LIST (HTML PAGE) - HIDDEN ================= */
	@Hidden // លាក់ HTML Page
	@GetMapping
	public String listProducts(Model model) {
		model.addAttribute("products", productService.getAllProducts());
		return "products/index";
	}

	/* ================= LIST (JSON) ================= */
	@Operation(summary = "List all products", description = "Returns all products as a JSON array")
	@ApiResponses({ 
		@ApiResponse(
			responseCode = "200", 
			description = "Products returned",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Product.class))
		) 
	})
	@GetMapping("/api")
	@ResponseBody
	public List<Product> listProductsJson() {
		return productService.getAllProducts();
	}

	/* ================= ADD (HTML FORM) - HIDDEN ================= */
	@Hidden // លាក់ HTML Form
	@GetMapping("/add")
	public String showAddForm(Model model) {
		model.addAttribute("product", new Product());
		model.addAttribute("categories", categoryRepository.findAll());
		model.addAttribute("suppliers", supplierRepository.findAll());
		return "products/add_product";
	}

	/* ================= ADD (POST ACTION) ================= */
	@Operation(
		summary = "Create a new product",
		requestBody = @RequestBody(
			description = "Product object to create",
			required = true,
			content = @Content(
				mediaType = MediaType.APPLICATION_JSON_VALUE,
				schema = @Schema(implementation = Product.class)
			)
		)
	)
	@ApiResponses({ @ApiResponse(responseCode = "302", description = "Redirects to /products after creation") })
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

	/* ================= EDIT (HTML PAGE) - HIDDEN ================= */
	@Hidden // លាក់ HTML Form
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

	/* ================= EDIT (JSON) ================= */
	@Operation(summary = "Get product data for editing", description = "Returns single product details as JSON")
	@ApiResponses({ 
		@ApiResponse(
			responseCode = "200", 
			description = "Product returned",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Product.class))
		),
		@ApiResponse(responseCode = "404", description = "Product not found") 
	})
	@GetMapping("/edit/{id}/api")
	@ResponseBody
	public ResponseEntity<Product> showEditFormJson(
			@Parameter(description = "ID of the product to edit") @PathVariable Long id) {
		return productService.getProductById(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	/* ================= UPDATE (PUT ACTION) ================= */
	@Operation(
		summary = "Update an existing product",
		requestBody = @RequestBody(
			description = "Updated product object",
			required = true,
			content = @Content(
				mediaType = MediaType.APPLICATION_JSON_VALUE,
				schema = @Schema(implementation = Product.class)
			)
		)
	)
	@PutMapping("/edit/{id}")
	public String updateProduct(@PathVariable Long id,
			@ModelAttribute Product product, @RequestParam("category_id") Long categoryId,
			@RequestParam("supplier_id") Long supplierId, RedirectAttributes redirectAttributes) {

		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + categoryId));
		Supplier supplier = supplierRepository.findById(supplierId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid supplier ID: " + supplierId));

		product.setId(id);
		product.setCategory(category);
		product.setSupplier(supplier);

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

	/* ================= DELETE (SINGLE) ================= */
	@Operation(summary = "Delete a single product", description = "Fails if the product is used in existing sales or purchases")
	@ApiResponses({
			@ApiResponse(responseCode = "302", description = "Redirects to /products with a success or error flash message") })
	@DeleteMapping("/delete/{id}")
	public String deleteProduct(@Parameter(description = "ID of the product to delete") @PathVariable Long id,
			RedirectAttributes redirectAttributes) {
		Optional<Product> optionalProduct = productRepository.findById(id);

		if (optionalProduct.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Product not found!");
			return "redirect:/products";
		}

		Product product = optionalProduct.get();

		boolean hasSales = productService.hasSales(product.getId());
		boolean hasPurchases = productService.hasPurchases(product.getId());

		if (hasSales || hasPurchases) {
			redirectAttributes.addFlashAttribute("errorMessage",
					"Cannot delete product! It is used in existing sales or purchases.");
			return "redirect:/products";
		}

		productService.deleteProduct(id);
		redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully!");
		return "redirect:/products";
	}

	/* ================= DELETE (MULTIPLE) ================= */
	@Operation(summary = "Delete multiple products", description = "Products referenced by sales or purchases are skipped and reported separately")
	@ApiResponses({
			@ApiResponse(responseCode = "302", description = "Redirects to /products with a summary flash message") })
	@DeleteMapping("/delete-multiple")
	public String deleteMultipleProducts(
			@Parameter(description = "IDs of the products to delete") @RequestParam(value = "productIds", required = false) List<Long> ids,
			RedirectAttributes redirectAttributes) {
		if (ids == null || ids.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "No products were selected for deletion.");
			return "redirect:/products";
		}

		List<Long> deletableIds = new ArrayList<>();
		int restrictedCount = 0;

		for (Long id : ids) {
			boolean hasSales = productService.hasSales(id);
			boolean hasPurchases = productService.hasPurchases(id);

			if (hasSales || hasPurchases) {
				restrictedCount++;
			} else {
				deletableIds.add(id);
			}
		}

		if (!deletableIds.isEmpty()) {
			productService.deleteAllById(deletableIds);
		}

		if (restrictedCount > 0 && !deletableIds.isEmpty()) {
			redirectAttributes.addFlashAttribute("successMessage",
					"Successfully deleted " + deletableIds.size() + " products.");
			redirectAttributes.addFlashAttribute("errorMessage",
					restrictedCount + " products could not be deleted because they are linked to sales or purchases.");
		} else if (restrictedCount > 0) {
			redirectAttributes.addFlashAttribute("errorMessage",
					"Selected products cannot be deleted! They are used in existing sales or purchases.");
		} else {
			redirectAttributes.addFlashAttribute("successMessage", "Selected products deleted successfully!");
		}

		return "redirect:/products";
	}

	/* ================= STOCK ADJUSTMENT ================= */
	@Operation(summary = "Adjust product stock", description = "Increases or decreases product quantity (IN/OUT)")
	@PostMapping("/stock-adjust")
	public String adjustStock(@RequestParam("productId") Long productId,
			@RequestParam("movementType") String movementType, @RequestParam("quantity") Integer quantity,
			@RequestParam(value = "reason", required = false) String reason,
			@RequestParam(value = "note", required = false) String note, RedirectAttributes redirectAttributes) {

		Optional<Product> optionalProduct = productRepository.findById(productId);
		if (optionalProduct.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", " Product not found!");
			return "redirect:/products";
		}

		Product product = optionalProduct.get();

		int currentQty = product.getQuantity();
		int newQty = "IN".equalsIgnoreCase(movementType) ? currentQty + quantity : currentQty - quantity;

		if (newQty < 0) {
			redirectAttributes.addFlashAttribute("errorMessage", " Cannot deduct more stock than available!");
			return "redirect:/products";
		}

		product.setQuantity(newQty);

		if (newQty == 0 && !"Inactive".equals(product.getStatus())) {
			product.setStatus("Out of Stock");
		} else if (newQty > 0 && "Out of Stock".equals(product.getStatus())) {
			product.setStatus("Active");
		}

		productService.saveProduct(product);

		// Record the movement for stock history
		StockMovement movement = new StockMovement(product, movementType.toUpperCase(), quantity, reason, note,
				LocalDateTime.now(), newQty);
		stockMovementRepository.save(movement);

		redirectAttributes.addFlashAttribute("successMessage", " Stock adjusted successfully!");
		return "redirect:/products";
	}

	/* ================= STOCK HISTORY (HTML FRAGMENT) - HIDDEN ================= */
	@Hidden // លាក់ HTML Fragment
	@GetMapping("/stock-history/{id}")
	public String stockHistory(@PathVariable Long id, Model model) {
		List<StockMovement> movements = stockMovementRepository.findByProductIdOrderByDateDesc(id);
		model.addAttribute("movements", movements);
		return "products/stock-history";
	}

	/* ================= STOCK HISTORY (JSON) ================= */
	@Operation(summary = "Get stock movement history for a product", description = "Returns all IN/OUT stock movements for a product in JSON format")
	@ApiResponses({ 
		@ApiResponse(
			responseCode = "200", 
			description = "Stock movements returned",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = StockMovement.class))
		) 
	})
	@GetMapping("/stock-history/{id}/api")
	@ResponseBody
	public List<StockMovement> stockHistoryJson(@Parameter(description = "ID of the product") @PathVariable Long id) {
		return stockMovementRepository.findByProductIdOrderByDateDesc(id);
	}
}