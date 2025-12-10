package com.krsm.repository;


import com.krsm.entity.Supplier;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

	boolean existsByEmail(String email);

}
