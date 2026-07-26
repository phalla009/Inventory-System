package com.krsm.controller;

import com.krsm.entity.Purchases;
import com.krsm.repository.PurchasesRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/purchase-summary")
public class PurchaseSummaryController {

    private final PurchasesRepository purchaseRepository;

    public PurchaseSummaryController(PurchasesRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    @GetMapping
    public String showSummary(
            @RequestParam(required = false) Integer week,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String supplier,
            Model model) {

        List<Purchases> purchaseList = purchaseRepository.findAll().stream()
                .filter(p -> p.getPurchase_date() != null)
                .filter(p -> week == null || getWeekOfYear(p.getPurchase_date()) == week)
                .filter(p -> month == null || p.getPurchase_date().getMonthValue() == month)
                .filter(p -> year == null || p.getPurchase_date().getYear() == year)
                .filter(p -> supplier == null || supplier.isEmpty()
                        || (p.getSupplier() != null && p.getSupplier().getName() != null
                                && p.getSupplier().getName().toLowerCase().contains(supplier.toLowerCase())))
                .collect(Collectors.toList());

        double totalPrice = purchaseList.stream()
                .mapToDouble(p -> p.getTotal_amount() != null ? p.getTotal_amount() : 0).sum();
        int totalQty = purchaseList.stream().mapToInt(Purchases::getQuantity).sum();
        long totalSupplier = purchaseList.stream()
                .filter(p -> p.getSupplier() != null)
                .map(p -> p.getSupplier().getName()).distinct().count();

        model.addAttribute("purchaseList", purchaseList);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("totalQty", totalQty);
        model.addAttribute("totalSupplier", totalSupplier);

        // Select box options
        model.addAttribute("weeks", IntStream.rangeClosed(1, 53).boxed().toList());
        model.addAttribute("months", IntStream.rangeClosed(1, 12).boxed().toList());
        model.addAttribute("years", IntStream.rangeClosed(2025, 2050).boxed().toList());

        model.addAttribute("selectedWeek", week);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedSupplier", supplier);

        return "purchase_summary/index";
    }

    @GetMapping("/export-excel")
    public void exportExcel(
            @RequestParam(required = false) Integer week,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String supplier,
            HttpServletResponse response) throws IOException {

        List<Purchases> purchaseList = purchaseRepository.findAll().stream()
                .filter(p -> p.getPurchase_date() != null)
                .filter(p -> week == null || getWeekOfYear(p.getPurchase_date()) == week)
                .filter(p -> month == null || p.getPurchase_date().getMonthValue() == month)
                .filter(p -> year == null || p.getPurchase_date().getYear() == year)
                .filter(p -> supplier == null || supplier.isEmpty()
                        || (p.getSupplier() != null && p.getSupplier().getName() != null
                                && p.getSupplier().getName().toLowerCase().contains(supplier.toLowerCase())))
                .sorted(Comparator.comparing(Purchases::getPurchase_date))
                .collect(Collectors.toList());

        // Group by calendar date (day), sorted ascending
        Map<LocalDate, List<Purchases>> grouped = purchaseList.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getPurchase_date().toLocalDate(),
                        TreeMap::new,
                        Collectors.toList()));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Purchase Summary");

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
            String[] headers = {"ID", "Purchase Date", "Product", "Quantity", "Total Amount ($)", "Supplier"};

            // Title (reflects active filters)
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            StringBuilder titleText = new StringBuilder("Purchase Summary Report (Grouped by Date)");
            if (week != null || month != null || year != null || (supplier != null && !supplier.isEmpty())) {
                titleText.append(" — Filters:");
                if (year != null) titleText.append(" Year=").append(year);
                if (month != null) titleText.append(" Month=").append(month);
                if (week != null) titleText.append(" Week=").append(week);
                if (supplier != null && !supplier.isEmpty()) titleText.append(" Supplier=").append(supplier);
            }
            titleCell.setCellValue(titleText.toString());
            titleCell.setCellStyle(titleStyle);
            rowIdx++; // blank row

            double grandQty = 0, grandAmount = 0;
            int rowNum = 1;

            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            for (Map.Entry<LocalDate, List<Purchases>> entry : grouped.entrySet()) {
                LocalDate date = entry.getKey();
                List<Purchases> dayPurchases = entry.getValue();

                Row groupRow = sheet.createRow(rowIdx++);
                Cell groupCell = groupRow.createCell(0);
                groupCell.setCellValue("Date: " + date.format(dateFmt) + "  (" + dayPurchases.size() + " purchases)");
                groupCell.setCellStyle(dateGroupStyle);
                for (int c = 1; c < headers.length; c++) {
                    groupRow.createCell(c).setCellStyle(dateGroupStyle);
                }
                sheet.addMergedRegion(new CellRangeAddress(groupRow.getRowNum(), groupRow.getRowNum(), 0, headers.length - 1));

                Row headerRow = sheet.createRow(rowIdx++);
                for (int c = 0; c < headers.length; c++) {
                    Cell cell = headerRow.createCell(c);
                    cell.setCellValue(headers[c]);
                    cell.setCellStyle(headerStyle);
                }

                double dayQty = 0, dayAmount = 0;

                for (Purchases purchase : dayPurchases) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(rowNum++);
                    row.createCell(1).setCellValue(purchase.getPurchase_date().format(dateTimeFmt));
                    row.createCell(2).setCellValue(purchase.getProduct() != null ? purchase.getProduct().getName() : "N/A");
                    row.createCell(3).setCellValue(purchase.getQuantity());

                    Cell amtCell = row.createCell(4);
                    amtCell.setCellValue(purchase.getTotal_amount() != null ? purchase.getTotal_amount() : 0);
                    amtCell.setCellStyle(currencyStyle);

                    row.createCell(5).setCellValue(purchase.getSupplier() != null ? purchase.getSupplier().getName() : "N/A");

                    dayQty += purchase.getQuantity();
                    dayAmount += purchase.getTotal_amount() != null ? purchase.getTotal_amount() : 0;
                }

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

                Cell amtCell = subtotalRow.createCell(4);
                amtCell.setCellValue(dayAmount);
                amtCell.setCellStyle(subtotalStyle);

                subtotalRow.createCell(5).setCellStyle(subtotalStyle);

                grandQty += dayQty;
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

            Cell gAmt = grandRow.createCell(4);
            gAmt.setCellValue(grandAmount);
            gAmt.setCellStyle(headerStyle);

            grandRow.createCell(5).setCellStyle(headerStyle);

            for (int c = 0; c < headers.length; c++) {
                sheet.autoSizeColumn(c);
            }

            String filename = "purchase_summary_" + LocalDate.now().format(dateFmt) + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            workbook.write(response.getOutputStream());
            response.flushBuffer();
        }
    }

    private int getWeekOfYear(java.time.LocalDateTime date) {
        return date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }
}