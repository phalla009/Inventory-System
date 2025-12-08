package com.krsm.service;

import java.util.List;

import com.krsm.entity.Sales;
import com.krsm.repository.SaleRepository;

public class SaleService {

	private static SaleRepository saleRepository;

	public SaleService(SaleRepository saleRepository) {
		this.saleRepository = saleRepository;
	}

	public static List<Sales> findByIds(List<Long> idList) {
		return saleRepository.findAllById(idList);
	}

}
