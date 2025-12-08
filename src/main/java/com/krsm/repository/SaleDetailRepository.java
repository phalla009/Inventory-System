package com.krsm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.krsm.entity.SaleDetail;
import com.krsm.entity.Sales;

public interface SaleDetailRepository {

	@Query("SELECT d FROM sale_detail d JOIN FETCH d.product WHERE d.sale = :sale")
	static
	List<SaleDetail> findBySale(@Param("sale") Sales sale) {
		// TODO Auto-generated method stub
		return null;
	}

//	public static List<SaleDetail> findBySaleId(Long id) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	public static List<SaleDetail> findBySale(Sales sale) {
//		// TODO Auto-generated method stub
//		return null;
//	}

}
