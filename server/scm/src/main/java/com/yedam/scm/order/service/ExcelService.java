package com.yedam.scm.order.service;

import java.io.ByteArrayOutputStream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.io.InputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import com.yedam.scm.order.mapper.OrderMapper;
import com.yedam.scm.vo.SalesOrderDetailVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExcelService {

    private final OrderMapper orderMapper;

    public byte[] generateOrderExcel(String orderId) throws IOException {

        // 주문 상세 조회
        List<SalesOrderDetailVO> details = orderMapper.findOrderDetails(orderId);

        // 엑셀 생성
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("주문내역");

        // ===================== 스타일 정의 =====================
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        CellStyle bodyStyle = workbook.createCellStyle();
        bodyStyle.setAlignment(HorizontalAlignment.CENTER);
        bodyStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        bodyStyle.setBorderBottom(BorderStyle.THIN);
        bodyStyle.setBorderTop(BorderStyle.THIN);
        bodyStyle.setBorderLeft(BorderStyle.THIN);
        bodyStyle.setBorderRight(BorderStyle.THIN);

        // ===================== 로고 추가 (배포 안정형 최종본) =====================
        try (InputStream logoStream = this.getClass().getResourceAsStream("/reports/logo.png")) {
            if (logoStream == null) {
                throw new FileNotFoundException("reports/logo.png not found in classpath.");
            }

            byte[] imageBytes = logoStream.readAllBytes();
            int pictureIdx = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);

            XSSFDrawing drawing = sheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = new XSSFClientAnchor();
            anchor.setCol1(0);
            anchor.setRow1(0);
            anchor.setCol2(2);
            anchor.setRow2(4);

            drawing.createPicture(anchor, pictureIdx);

        } catch (Exception e) {
            System.out.println("⚠️ 로고 삽입 실패: " + e.getMessage());
        }
        // ===================== 상단 제목 및 주문정보 =====================
        Row titleRow = sheet.createRow(1);
        Cell titleCell = titleRow.createCell(3);
        titleCell.setCellValue("YEDAM SCM 주문 내역서");
        titleCell.setCellStyle(titleStyle);

        Row infoRow1 = sheet.createRow(3);
        infoRow1.createCell(3).setCellValue("주문번호: " + orderId);
        infoRow1.createCell(5).setCellValue("주문일자: 2025-10-25");

        Row infoRow2 = sheet.createRow(4);
        infoRow2.createCell(3).setCellValue("가맹점명: 스타벅스 강남점");
        infoRow2.createCell(5).setCellValue("연락처: 02-555-9999");

        Row infoRow3 = sheet.createRow(5);
        infoRow3.createCell(3).setCellValue("주소: 서울특별시 강남구 테헤란로 123");

        // ===================== 테이블 헤더 =====================
        int rowIdx = 7;
        Row header = sheet.createRow(rowIdx++);
        String[] headers = {"제품코드", "제품명", "규격", "단위", "제품단가", "주문수량", "주문금액"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // ===================== 데이터 =====================
        long total = 0L;
        for (SalesOrderDetailVO d : details) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(d.getProdId());
            row.createCell(1).setCellValue(d.getProdName());
            row.createCell(2).setCellValue(d.getSpec());
            row.createCell(3).setCellValue(d.getUnit());
            row.createCell(4).setCellValue(d.getProdUnitPrice());
            row.createCell(5).setCellValue(d.getOrderQty());
            long lineTotal = d.getProdUnitPrice() * d.getOrderQty();
            row.createCell(6).setCellValue(lineTotal);
            total += lineTotal;

            for (int i = 0; i < 7; i++) {
                row.getCell(i).setCellStyle(bodyStyle);
            }
        }

        // ===================== 총 주문금액 =====================
        Row totalRow = sheet.createRow(rowIdx + 1);
        Cell totalLabel = totalRow.createCell(5);
        totalLabel.setCellValue("총 주문금액");
        totalLabel.setCellStyle(headerStyle);

        Cell totalValue = totalRow.createCell(6);
        totalValue.setCellValue(total);
        totalValue.setCellStyle(bodyStyle);

        // ===================== 열 너비 자동조정 =====================
        for (int i = 0; i < 7; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 800); // 여백 추가
        }

        // ===================== 출력 =====================
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }
}
