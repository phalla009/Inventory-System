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

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

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
    @GetMapping("/export-excel")
    public void exportExcel(
            @RequestParam(required = false) Integer week,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String customer,
            HttpServletResponse response) throws IOException {

        List<Sales> salesList = saleRepository.findAll().stream()
                .filter(s -> week == null || getWeekOfYear(s.getSale_date()) == week)
                .filter(s -> month == null || s.getSale_date().getMonthValue() == month)
                .filter(s -> year == null || s.getSale_date().getYear() == year)
                .filter(s -> customer == null || customer.isEmpty()
                        || s.getCustomer_name().toLowerCase().contains(customer.toLowerCase()))
                .sorted(Comparator.comparing(Sales::getSale_date))
                .collect(Collectors.toList());

        // Group by calendar date (day), sorted ascending via TreeMap
        Map<LocalDate, List<Sales>> grouped = salesList.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getSale_date().toLocalDate(),
                        TreeMap::new,
                        Collectors.toList()));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sales Summary");

            // ---- Styles ----
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle dateGroupStyle = workbook.createCellStyle();
            Font dateGroupFont = workbook.createFont();
            dateGroupFont.setBold(true);
            dateGroupStyle.setFont(dateGroupFont);
            dateGroupStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            dateGroupStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle subtotalStyle = workbook.createCellStyle();
            Font subtotalFont = workbook.createFont();
            subtotalFont.setBold(true);
            subtotalStyle.setFont(subtotalFont);
            subtotalStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            subtotalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            int rowIdx = 0;
            String[] headers = {"ID", "Date", "Product", "Quantity", "SubTotal ($)", "Discount", "Total Amount ($)", "Customer"};

            // Title
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Sales Summary Report (Grouped by Date)");
            titleCell.setCellStyle(titleStyle);
            rowIdx++; // blank row

            double grandQty = 0, grandSubtotal = 0, grandAmount = 0;
            int rowNum = 1;

            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            for (Map.Entry<LocalDate, List<Sales>> entry : grouped.entrySet()) {
                LocalDate date = entry.getKey();
                List<Sales> daySales = entry.getValue();

                // Date group header (merged across all columns)
                Row groupRow = sheet.createRow(rowIdx++);
                Cell groupCell = groupRow.createCell(0);
                groupCell.setCellValue("Date: " + date.format(dateFmt) + "  (" + daySales.size() + " sales)");
                groupCell.setCellStyle(dateGroupStyle);
                for (int c = 1; c < headers.length; c++) {
                    groupRow.createCell(c).setCellStyle(dateGroupStyle);
                }
                sheet.addMergedRegion(new CellRangeAddress(groupRow.getRowNum(), groupRow.getRowNum(), 0, headers.length - 1));

                // Column headers for this group
                Row headerRow = sheet.createRow(rowIdx++);
                for (int c = 0; c < headers.length; c++) {
                    Cell cell = headerRow.createCell(c);
                    cell.setCellValue(headers[c]);
                    cell.setCellStyle(headerStyle);
                }

                double dayQty = 0, daySubtotal = 0, dayAmount = 0;

                for (Sales sale : daySales) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(rowNum++);
                    row.createCell(1).setCellValue(sale.getSale_date().format(dateTimeFmt));
                    row.createCell(2).setCellValue(sale.getProduct() != null ? sale.getProduct().getName() : "N/A");
                    row.createCell(3).setCellValue(sale.getQuantity());

                    Cell subCell = row.createCell(4);
                    subCell.setCellValue(sale.getSubtotal());
                    subCell.setCellStyle(currencyStyle);

                    row.createCell(5).setCellValue(sale.getDiscount() + "%");

                    Cell totalCell = row.createCell(6);
                    totalCell.setCellValue(sale.getTotal_amount());
                    totalCell.setCellStyle(currencyStyle);

                    row.createCell(7).setCellValue(sale.getCustomer_name());

                    dayQty += sale.getQuantity();
                    daySubtotal += sale.getSubtotal();
                    dayAmount += sale.getTotal_amount();
                }

                // Subtotal row for this date
                Row subtotalRow = sheet.createRow(rowIdx++);
                Cell labelCell = subtotalRow.createCell(0);
                labelCell.setCellValue("Subtotal for " + date.format(dateFmt));
                labelCell.setCellStyle(subtotalStyle);
                sheet.addMergedRegion(new CellRangeAddress(subtotalRow.getRowNum(), subtotalRow.getRowNum(), 0, 2));
                subtotalRow.createCell(1).setCellStyle(subtotalStyle);
                subtotalRow.createCell(2).setCellStyle(subtotalStyle);

                Cell qtyCell = subtotalRow.createCell(3);
                qtyCell.setCellValue(dayQty);
                qtyCell.setCellStyle(subtotalStyle);

                Cell subCell = subtotalRow.createCell(4);
                subCell.setCellValue(daySubtotal);
                subCell.setCellStyle(subtotalStyle);

                subtotalRow.createCell(5).setCellStyle(subtotalStyle);

                Cell amtCell = subtotalRow.createCell(6);
                amtCell.setCellValue(dayAmount);
                amtCell.setCellStyle(subtotalStyle);

                subtotalRow.createCell(7).setCellStyle(subtotalStyle);

                grandQty += dayQty;
                grandSubtotal += daySubtotal;
                grandAmount += dayAmount;

                rowIdx++; // blank row between date groups
            }

            // Grand total row
            Row grandRow = sheet.createRow(rowIdx++);
            Cell grandLabel = grandRow.createCell(0);
            grandLabel.setCellValue("GRAND TOTAL");
            grandLabel.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(grandRow.getRowNum(), grandRow.getRowNum(), 0, 2));
            grandRow.createCell(1).setCellStyle(headerStyle);
            grandRow.createCell(2).setCellStyle(headerStyle);

            Cell gQty = grandRow.createCell(3);
            gQty.setCellValue(grandQty);
            gQty.setCellStyle(headerStyle);

            Cell gSub = grandRow.createCell(4);
            gSub.setCellValue(grandSubtotal);
            gSub.setCellStyle(headerStyle);

            grandRow.createCell(5).setCellStyle(headerStyle);

            Cell gAmt = grandRow.createCell(6);
            gAmt.setCellValue(grandAmount);
            gAmt.setCellStyle(headerStyle);

            grandRow.createCell(7).setCellStyle(headerStyle);

            for (int c = 0; c < headers.length; c++) {
                sheet.autoSizeColumn(c);
            }

            String filename = "sales_summary_" + LocalDate.now().format(dateFmt) + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            workbook.write(response.getOutputStream());
            response.flushBuffer();
        }
    }
}
