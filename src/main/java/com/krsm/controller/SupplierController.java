package com.krsm.controller;

import com.krsm.entity.Supplier;
import com.krsm.service.SupplierService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/suppliers")
@Tag(name = "Supplier", description = "Endpoints for managing product suppliers")
public class SupplierController {

	private final SupplierService service;

	public SupplierController(SupplierService service) {
		this.service = service;
	}

	/* ================= LIST (HTML PAGE) - HIDDEN ================= */
	@Hidden // លាក់ HTML Page
	@GetMapping
	public String listSuppliers(Model model) {
		model.addAttribute("suppliers", service.getAllSuppliers());
		return "suppliers/index";
	}

	/* ================= LIST (JSON) ================= */
	@Operation(summary = "List all suppliers", description = "Returns all suppliers as a JSON array")
	@ApiResponses({ 
		@ApiResponse(
			responseCode = "200", 
			description = "Suppliers returned",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Supplier.class))
		) 
	})
	@GetMapping("/api")
	@ResponseBody
	public List<Supplier> listSuppliersJson() {
		return service.getAllSuppliers();
	}

	/* ================= ADD (HTML FORM) - HIDDEN ================= */
	@Hidden // លាក់ HTML Form
	@GetMapping("/add")
	public String showAddForm(Model model) {
		model.addAttribute("supplier", new Supplier());
		return "suppliers/add_supplier";
	}

	/* ================= ADD (POST ACTION) ================= */
	@Operation(
		summary = "Create a new supplier",
		requestBody = @RequestBody(
			description = "JSON payload for creating a supplier",
			required = true,
			content = @Content(
				mediaType = MediaType.APPLICATION_JSON_VALUE,
				schema = @Schema(implementation = Supplier.class),
				examples = @ExampleObject(
					name = "New Supplier Example",
					value = "{\n  \"name\": \"Acme Corp\",\n  \"gender\": \"Male\",\n  \"email\": \"acme@example.com\",\n  \"phone\": \"012345678\",\n  \"address\": \"Phnom Penh\"\n}"
				)
			)
		)
	)
	@ApiResponses({ @ApiResponse(responseCode = "302", description = "Redirects to /suppliers after creation") })
	@PostMapping("/add")
	public String addSupplier(@ModelAttribute Supplier supplier, RedirectAttributes redirectAttributes) {
		service.saveSupplier(supplier);
		redirectAttributes.addFlashAttribute("successMessage", "Supplier added successfully!");
		return "redirect:/suppliers";
	}

	/* ================= EDIT (HTML FORM) - HIDDEN ================= */
	@Hidden // លាក់ HTML Form
	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
		Optional<Supplier> optionalSupplier = service.getSupplierById(id);
		if (optionalSupplier.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "⚠️ Supplier not found!");
			return "redirect:/suppliers";
		}
		model.addAttribute("supplier", optionalSupplier.get());
		return "suppliers/edit_supplier";
	}

	/* ================= EDIT (JSON) ================= */
	@Operation(summary = "Get supplier data for editing", description = "Returns single supplier details as JSON")
	@ApiResponses({ 
		@ApiResponse(
			responseCode = "200", 
			description = "Supplier returned",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Supplier.class))
		),
		@ApiResponse(responseCode = "404", description = "Supplier not found") 
	})
	@GetMapping("/edit/{id}/api")
	@ResponseBody
	public ResponseEntity<Supplier> showEditFormJson(
			@Parameter(description = "ID of the supplier to edit") @PathVariable Long id) {
		return service.getSupplierById(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	/* ================= UPDATE (PUT ACTION) ================= */
	@Operation(
		summary = "Update an existing supplier",
		requestBody = @RequestBody(
			description = "JSON payload for updating a supplier",
			required = true,
			content = @Content(
				mediaType = MediaType.APPLICATION_JSON_VALUE,
				schema = @Schema(implementation = Supplier.class),
				examples = @ExampleObject(
					name = "Update Supplier Example",
					value = "{\n  \"name\": \"Acme Corp Updated\",\n  \"gender\": \"Male\",\n  \"email\": \"contact@acme.com\",\n  \"phone\": \"012345678\",\n  \"address\": \"Phnom Penh\"\n}"
				)
			)
		)
	)
	@PutMapping("/edit/{id}")
	public String updateSupplier(@PathVariable Long id, @ModelAttribute Supplier supplier, RedirectAttributes redirectAttributes) {
		supplier.setId(id);
		service.saveSupplier(supplier);
		redirectAttributes.addFlashAttribute("successMessage", "Supplier updated successfully!");
		return "redirect:/suppliers";
	}

	/* ================= DELETE (CONFIRM FORM - HTML PAGE) - HIDDEN ================= */
	@Hidden // លាក់ HTML Page
	@GetMapping("/delete/{id}")
	public String showDeleteForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
		Optional<Supplier> optionalSupplier = service.getSupplierById(id);
		if (optionalSupplier.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "⚠️ Supplier not found!");
			return "redirect:/suppliers";
		}
		model.addAttribute("supplier", optionalSupplier.get());
		return "suppliers/delete_supplier";
	}

	/* ================= DELETE (CONFIRM FORM - JSON) ================= */
	@Operation(summary = "Get supplier data for delete confirmation", description = "Returns supplier details in JSON before confirmation")
	@ApiResponses({ 
		@ApiResponse(
			responseCode = "200", 
			description = "Supplier returned",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Supplier.class))
		),
		@ApiResponse(responseCode = "404", description = "Supplier not found") 
	})
	@GetMapping("/delete/{id}/api")
	@ResponseBody
	public ResponseEntity<Supplier> showDeleteFormJson(
			@Parameter(description = "ID of the supplier to delete") @PathVariable Long id) {
		return service.getSupplierById(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	/* ================= DELETE (SINGLE) ================= */
	@Operation(summary = "Delete a single supplier", description = "Fails if the supplier is still referenced by existing products")
	@ApiResponses({
			@ApiResponse(responseCode = "302", description = "Redirects to /suppliers with a success or error flash message") })
	@DeleteMapping("/delete/{id}")
	public String deleteSupplier(@Parameter(description = "ID of the supplier to delete") @PathVariable Long id,
			RedirectAttributes redirectAttributes) {
		Optional<Supplier> optionalSupplier = service.getSupplierById(id);

		if (optionalSupplier.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Supplier not found!");
			return "redirect:/suppliers";
		}

		boolean hasProducts = service.hasProducts(id);
		if (hasProducts) {
			redirectAttributes.addFlashAttribute("errorMessage",
					"❌ Cannot delete supplier! It is used by existing products.");
			return "redirect:/suppliers";
		}

		service.deleteSupplier(id);
		redirectAttributes.addFlashAttribute("successMessage", "Supplier deleted successfully!");
		return "redirect:/suppliers";
	}

	/* ================= DELETE (MULTIPLE) ================= */
	@Operation(summary = "Delete multiple suppliers", description = "Suppliers referenced by existing products are skipped and reported separately")
	@ApiResponses({
			@ApiResponse(responseCode = "302", description = "Redirects to /suppliers with a summary flash message") })
	@DeleteMapping("/delete-multiple")
	public String deleteMultipleSuppliers(
			@Parameter(description = "IDs of the suppliers to delete") @RequestParam(value = "supplierIds", required = false) List<Long> ids,
			RedirectAttributes redirectAttributes) {

		if (ids == null || ids.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "⚠️ No suppliers were selected.");
			return "redirect:/suppliers";
		}

		List<Long> deletableIds = new ArrayList<>();
		int restrictedCount = 0;

		for (Long id : ids) {
			if (service.hasProducts(id)) {
				restrictedCount++;
			} else {
				deletableIds.add(id);
			}
		}

		if (!deletableIds.isEmpty()) {
			service.deleteAllById(deletableIds);
		}

		if (restrictedCount > 0 && !deletableIds.isEmpty()) {
			redirectAttributes.addFlashAttribute("successMessage",
					"Successfully deleted " + deletableIds.size() + " supplier(s).");
			redirectAttributes.addFlashAttribute("errorMessage", restrictedCount
					+ " supplier(s) could not be deleted because they are assigned to active products.");
		} else if (restrictedCount > 0) {
			redirectAttributes.addFlashAttribute("errorMessage",
					"None of the selected suppliers could be deleted! They are assigned to active products.");
		} else {
			redirectAttributes.addFlashAttribute("successMessage", "Selected suppliers deleted successfully!");
		}

		return "redirect:/suppliers";
	}
}