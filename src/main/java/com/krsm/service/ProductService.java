package com.krsm.service;

import com.krsm.entity.Product;
import com.krsm.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
	public static final String ProductService = null;
	private final ProductRepository repo;

	public ProductService(ProductRepository repo) {
		this.repo = repo;
	}

	public List<Product> getAllProducts() {
		return repo.findAll();

	}

	public Optional<Product> getProductById(Long id) {
		return repo.findById(id);
	}

	public Product saveProduct(Product product) {
		return repo.save(product);
	}

	public void deleteProduct(Long id) {
		repo.deleteById(id);
	}

	public Product findById(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasSales(Long id) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasPurchases(Long id) {
		// TODO Auto-generated method stub
		return false;
	}
}
