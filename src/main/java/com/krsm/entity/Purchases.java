package com.krsm.entity;


import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Purchases {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int quantity;
    private double price;
    private double subtotal;
    private Double total_amount;
    private LocalDateTime purchase_date;
    private LocalDateTime created_at;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product; 

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
    
    @PrePersist
	public void prePersist() {
		if (created_at == null) {
			created_at = LocalDateTime.now();
		}
	}

	@PreUpdate
	public void preUpdate() {
		if (created_at == null) {
			created_at = LocalDateTime.now();
		}
	}
}


