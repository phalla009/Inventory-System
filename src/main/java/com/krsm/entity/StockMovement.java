package com.krsm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
public class StockMovement {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(nullable = false, length = 10)
	private String type; // "IN" or "OUT"

	@Column(nullable = false)
	private Integer quantity;

	private String reason;

	@Column(length = 500)
	private String note;

	@Column(nullable = false)
	private LocalDateTime date;

	// Quantity snapshot after this movement — handy for history display
	private Integer resultingQuantity;

	public StockMovement() {
	}

	public StockMovement(Product product, String type, Integer quantity, String reason, String note, LocalDateTime date,
			Integer resultingQuantity) {
		this.product = product;
		this.type = type;
		this.quantity = quantity;
		this.reason = reason;
		this.note = note;
		this.date = date;
		this.resultingQuantity = resultingQuantity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public Integer getResultingQuantity() {
		return resultingQuantity;
	}

	public void setResultingQuantity(Integer resultingQuantity) {
		this.resultingQuantity = resultingQuantity;
	}
}