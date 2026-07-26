package com.krsm.repository;

import com.krsm.entity.Product;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
	Optional<Product> findByNameIgnoreCase(String name);
}
