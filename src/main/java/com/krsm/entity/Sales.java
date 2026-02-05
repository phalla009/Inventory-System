package com.krsm.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sales {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDateTime sale_date;
	private String customer_name;

	@ManyToOne
	@JoinColumn(name = "product_id")
	private Product product;

	private int quantity;
	private double price;
	private double discount = 0.0;
	private double subtotal;
	private double total_amount;
	private LocalDateTime created_at;

	@OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SaleDetail> saleDetails;

	@PrePersist
	@PreUpdate
	public void calculateTotals() {
		this.subtotal = this.quantity * this.price;
		double discountAmount = this.subtotal * (this.discount / 100);
		this.total_amount = this.subtotal - discountAmount; // ✅ subtract discount
		if (this.created_at == null) {
			this.created_at = LocalDateTime.now();
		}
	}

}
