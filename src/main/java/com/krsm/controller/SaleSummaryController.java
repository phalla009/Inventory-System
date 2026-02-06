package com.krsm.controller;

import com.krsm.entity.Sales;
import com.krsm.repository.SaleRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.temporal.IsoFields;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/sales-summary")
public class SaleSummaryController {

    private final SaleRepository saleRepository;

    public SaleSummaryController(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @GetMapping
    public String showSummary(
            @RequestParam(required = false) Integer week,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String customer,  // ✅ new parameter
            Model model) {

        // Filter sales based on week/month/year/customer
        List<Sales> salesList = saleRepository.findAll().stream()
                .filter(s -> week == null || getWeekOfYear(s.getSale_date()) == week)
                .filter(s -> month == null || s.getSale_date().getMonthValue() == month)
                .filter(s -> year == null || s.getSale_date().getYear() == year)
                .filter(s -> customer == null || customer.isEmpty()
                        || s.getCustomer_name().toLowerCase().contains(customer.toLowerCase()))
                .collect(Collectors.toList());

        // Totals
        double totalPrice = salesList.stream().mapToDouble(Sales::getTotal_amount).sum();
        int totalQty = salesList.stream().mapToInt(Sales::getQuantity).sum();
        long totalCustomer = salesList.stream().map(Sales::getCustomer_name).distinct().count();

        model.addAttribute("salesList", salesList);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("totalQty", totalQty);
        model.addAttribute("totalCustomer", totalCustomer);

        // Select box options
        model.addAttribute("weeks", IntStream.rangeClosed(1, 53).boxed().toList());
        model.addAttribute("months", IntStream.rangeClosed(1, 12).boxed().toList());
        model.addAttribute("years", IntStream.rangeClosed(2025, 2050).boxed().toList());

        model.addAttribute("selectedWeek", week);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedCustomer", customer);  // ✅ keep value in input

        return "sales_summary/index";
    }

    // Utility: get week of year
    private int getWeekOfYear(java.time.LocalDateTime date) {
        return date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }
}
