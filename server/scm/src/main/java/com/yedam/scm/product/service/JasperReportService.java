package com.yedam.scm.product.service;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yedam.scm.dto.WonjangReportDTO;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
public class JasperReportService {

    public byte[] generateAccountLedgerReport(List<WonjangReportDTO> data) throws Exception {

            // ✅ 1️⃣ JRXML 템플릿 로드
        InputStream template = getClass().getResourceAsStream("/reports/account_ledger_report.jrxml");
    System.out.println(">>> JRXML template = " + (template != null ? "FOUND ✅" : "NOT FOUND ❌"));

    if (template == null) {
        throw new IllegalStateException("❌ JRXML not found: /reports/account_ledger_report.jrxml");
}

        // ✅ 2️⃣ JasperReport 컴파일
        JasperReport report = JasperCompileManager.compileReport(template);

        // ✅ 3️⃣ 데이터 소스
        JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(data);

        // ✅ 4️⃣ 리포트 채우기
        JasperPrint print = JasperFillManager.fillReport(report, Map.of("title", "거래처원장 리포트"), ds);

        // ✅ 5️⃣ PDF로 내보내기
        return JasperExportManager.exportReportToPdf(print);
    }
}
