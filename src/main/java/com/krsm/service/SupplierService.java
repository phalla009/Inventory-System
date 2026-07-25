package com.krsm.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.krsm.entity.Supplier;
import com.krsm.repository.ProductRepository;
import com.krsm.repository.SupplierRepository;

@Service
@Transactional
public class SupplierService {

	private final SupplierRepository supplierRepository;
	private final ProductRepository productRepository;

	public SupplierService(SupplierRepository supplierRepository, ProductRepository productRepository) {
		this.supplierRepository = supplierRepository;
		this.productRepository = productRepository;
	}

	/**
	 * Retrieve all suppliers from the database.
	 */
	@Transactional(readOnly = true)
	public List<Supplier> getAllSuppliers() {
		return supplierRepository.findAll();
	}

	/**
	 * Save or update a supplier entity.
	 */
	public void saveSupplier(Supplier supplier) {
		supplierRepository.save(supplier);
	}

	/**
	 * Find a supplier by its primary ID.
	 */
	@Transactional(readOnly = true)
	public Optional<Supplier> getSupplierById(Long id) {
		return supplierRepository.findById(id);
	}

	/**
	 * Check if any products are assigned to this supplier.
	 */
	@Transactional(readOnly = true)
	public boolean hasProducts(Long id) {
		return productRepository.existsById(id);
	}

	/**
	 * Delete a single supplier by ID.
	 */
	public void deleteSupplier(Long id) {
		supplierRepository.deleteById(id);
	}

	/**
	 * Batch delete multiple suppliers by their IDs.
	 */
	public void deleteAllById(List<Long> deletableIds) {
		supplierRepository.deleteAllById(deletableIds);
	}
}