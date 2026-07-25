package com.krsm.controller;

import com.krsm.entity.Product;
import com.krsm.entity.Sales;
import com.krsm.repository.ProductRepository;
import com.krsm.repository.SaleRepository;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/sales")
@Tag(name = "Sale", description = "Endpoints for managing sales transactions")
public class SaleController {

	private final SaleRepository saleRepository;
	private final ProductRepository productRepository;

	public SaleController(SaleRepository saleRepository, ProductRepository productRepository) {
		this.saleRepository = saleRepository;
		this.productRepository = productRepository;
	}

	/* ================= 1. LIST ALL SALES (HTML PAGE) - HIDDEN ================= */
	@Hidden
	@GetMapping
	public String listSales(Model model) {
		List<Sales> salesList = saleRepository.findAll();
		model.addAttribute("salesList", salesList);
		return "sales/index";
	}

	/* ================= LIST ALL SALES (JSON) ================= */
	@Hidden
	@Operation(summary = "List all sales", description = "Returns all sales transactions as a JSON array")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Sales retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Sales.class))) })
	@GetMapping("/api")
	@ResponseBody
	public List<Sales> listSalesJson() {
		return saleRepository.findAll();
	}

	/* ================= GET SALE BY ID (JSON) ================= */
	@Hidden
	@Operation(summary = "Get sale details by ID", description = "Returns single sale transaction details as JSON")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Sale found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Sales.class))),
			@ApiResponse(responseCode = "404", description = "Sale not found") })
	@GetMapping("/api/{id}")
	@ResponseBody
	public ResponseEntity<Sales> getSaleByIdJson(@Parameter(description = "ID of the sale") @PathVariable Long id) {
		return saleRepository.findById(id).map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	/* ================= 2. SHOW FORM TO ADD NEW SALE - HIDDEN ================= */
	@Hidden
	@GetMapping("/add")
	public String showAddForm(Model model) {
		model.addAttribute("sale", new Sales());
		model.addAttribute("productsList", productRepository.findAll());
		return "sales/add_sale";
	}

	/* ================= 3. SAVE NEW SALE ================= */
	@Hidden
	@Operation(summary = "Create a new sale transaction", requestBody = @RequestBody(description = "JSON payload for creating a sale", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Sales.class), examples = @ExampleObject(name = "New Sale Example", value = "{\n  \"customer_name\": \"Sokha\",\n  \"quantity\": 2,\n  \"price\": 15.0,\n  \"discount\": 5.0,\n  \"product\": {\n    \"id\": 1\n  }\n}"))))
	@ApiResponses({ @ApiResponse(responseCode = "302", description = "Redirects to /sales after creation"),
			@ApiResponse(responseCode = "400", description = "Insufficient stock or invalid product ID") })
	@PostMapping("/save")
	public String saveSale(@ModelAttribute("sale") Sales sale,
			@RequestParam(value = "productId", required = false) Long productId,
			RedirectAttributes redirectAttributes) {

		// Handle cases where productId might be passed inside the Sales object or via
		// request parameter
		Long targetProductId = (productId != null) ? productId
				: (sale.getProduct() != null ? sale.getProduct().getId() : null);

		if (targetProductId == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Product ID is required!");
			return "redirect:/sales";
		}

		Product product = productRepository.findById(targetProductId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid Product ID"));

		int newQty = product.getQuantity() - sale.getQuantity();
		if (newQty < 0) {
			redirectAttributes.addFlashAttribute("errorMessage", "Not enough stock!");
			return "redirect:/sales";
		}

		product.setQuantity(newQty);
		productRepository.save(product);

		sale.setProduct(product);
		sale.setCreated_at(LocalDateTime.now());

		// Recalculate if subtotal & total_amount need direct binding
		double subtotal = sale.getQuantity() * sale.getPrice();
		sale.setSubtotal(subtotal);
		double totalAmount = subtotal - (subtotal * sale.getDiscount() / 100);
		sale.setTotal_amount(totalAmount);

		saleRepository.save(sale);

		redirectAttributes.addFlashAttribute("successMessage", "Sale added successfully!");
		return "redirect:/sales";
	}

	/* ================= 4. SHOW FORM TO EDIT SALE - HIDDEN ================= */
	@Hidden
	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable("id") Long id, Model model) {
		Sales sale = saleRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid Sale ID: " + id));
		model.addAttribute("sale", sale);
		model.addAttribute("productsList", productRepository.findAll());
		return "sales/edit_sale";
	}

	/* ================= 5. UPDATE SALE WITH STOCK ADJUSTMENT ================= */
	@Hidden
	@Operation(summary = "Update an existing sale", requestBody = @RequestBody(description = "JSON payload for updating a sale", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Sales.class), examples = @ExampleObject(name = "Update Sale Example", value = "{\n  \"sale_date\": \"2026-07-25T10:00:00\",\n  \"customer_name\": \"Sokha Updated\",\n  \"quantity\": 3,\n  \"price\": 15.0,\n  \"discount\": 10.0,\n  \"product\": {\n    \"id\": 1\n  }\n}"))))
	@ApiResponses({ @ApiResponse(responseCode = "302", description = "Redirects to /sales after update"),
			@ApiResponse(responseCode = "400", description = "Update failed due to insufficient stock") })
	@PostMapping("/update/{id}")
	public String updateSale(@PathVariable("id") Long id, @ModelAttribute("sale") Sales saleDetails,
			RedirectAttributes redirectAttributes) {

		Sales sale = saleRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid Sale ID: " + id));

		// Fetch full product from DB
		Product product = productRepository.findById(saleDetails.getProduct().getId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid Product ID"));

		// Calculate stock difference
		int qtyDiff = saleDetails.getQuantity() - sale.getQuantity();
		int newQty = product.getQuantity() - qtyDiff;
		if (newQty < 0) {
			redirectAttributes.addFlashAttribute("errorMessage", "Update failed! Not enough stock!");
			return "redirect:/sales";
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
		sale.setDiscount(saleDetails.getDiscount());

		// Recalculate subtotal and total_amount
		double subtotal = sale.getQuantity() * sale.getPrice();
		sale.setSubtotal(subtotal);
		double totalAmount = subtotal - (subtotal * sale.getDiscount() / 100);
		sale.setTotal_amount(totalAmount);

		saleRepository.save(sale);

		redirectAttributes.addFlashAttribute("successMessage", "Sale updated successfully!");
		return "redirect:/sales";
	}

	/* ================= 6. DELETE SALE AND RESTORE STOCK ================= */
	@Hidden
	@Operation(summary = "Delete a sale transaction and restore product stock")
	@ApiResponses({ @ApiResponse(responseCode = "302", description = "Redirects to /sales after deletion") })
	@PostMapping("/delete/{id}")
	public String deleteSale(@Parameter(description = "ID of the sale to delete") @PathVariable("id") Long id,
			RedirectAttributes redirectAttributes) {
		Sales sale = saleRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid Sale ID: " + id));

		// Restore stock
		Product product = sale.getProduct();
		product.setQuantity(product.getQuantity() + sale.getQuantity());
		productRepository.save(product);

		saleRepository.delete(sale);
		redirectAttributes.addFlashAttribute("successMessage", "Sale deleted successfully!");
		return "redirect:/sales";
	}

	/* ================= 7. BATCH PRINT PAGE - HIDDEN ================= */
	@Hidden
	@GetMapping("/print/batch")
	public String printBatch(@RequestParam("ids") String ids, Model model) {
		List<Long> saleIds = Arrays.stream(ids.split(",")).map(Long::parseLong).toList();
		List<Sales> selectedSales = saleRepository.findAllById(saleIds);

		model.addAttribute("selectedSales", selectedSales);
		return "sales/batch_print";
	}
}