package com.krsm.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String description;
	private int quantity;
	private double price;
	private String status;
	private LocalDateTime create_at;

	@ManyToOne
	@JoinColumn(name = "category_id")
	private Category category;
	@ManyToOne
	@JoinColumn(name = "supplier_id")
	private Supplier supplier;

	@PrePersist
	public void prePersist() {
		if (create_at == null) {
			create_at = LocalDateTime.now();
		}
	}

	@PreUpdate
	public void preUpdate() {
		if (create_at == null) {
			create_at = LocalDateTime.now();
		}
	}

	public String getSales() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPurchases() {
		// TODO Auto-generated method stub
		return null;
	}
}
