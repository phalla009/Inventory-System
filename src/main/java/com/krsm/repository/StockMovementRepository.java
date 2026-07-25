package com.krsm.repository;

import com.krsm.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

	List<StockMovement> findByProductIdOrderByDateDesc(Long productId);
}