package com.yedam.scm.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.scm.dto.AccountLedgerSearchDTO;
import com.yedam.scm.dto.InboundListRes;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.dto.WarehouseListRes;
import com.yedam.scm.dto.WonjangReportDTO;
import com.yedam.scm.product.service.InboundService;
import com.yedam.scm.product.service.JasperReportService;
import com.yedam.scm.vo.ItemInboundVO;
import com.yedam.scm.vo.ReturnDetailVO;
import com.yedam.scm.vo.ReturnVO;
import com.yedam.scm.vo.SalesOrderDetailVO;
import com.yedam.scm.vo.SalesOrderVO;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.base-path:/}")
public class DwController {

    private final InboundService service;

    // pdf 재스퍼 추가
    private final JasperReportService jasperService;

    /* ===================== 제품입고 ===================== */

    @GetMapping("/lots")
    public List<ItemInboundVO> getInboundLots(ItemInboundVO vo) {
        return service.selectInboundLots(vo);
    }

    @PostMapping("/lots")
    public Map<String, Object> insertInbound(@RequestBody Map<String, Object> vo) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", service.registerInbound(vo) ? "success" : "fail");
        return response;
    }

    @DeleteMapping("/lots/{inboundId}")
    public int deleteInbound(@PathVariable String inboundId) {
        return service.deleteInbound(inboundId);
    }

    @PostMapping("/inbound")
    public ResponseEntity<Map<String, Object>> registerInbound(@RequestBody Map<String, Object> inbound) {
        boolean isRegister = service.registerInbound(inbound);
        Map<String, Object> response = new HashMap<>();
        response.put("retCode", isRegister ? "success" : "BAD_REQUEST");
        return isRegister
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @GetMapping("/inbound/product")
    public InboundListRes getInboundProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        PageDTO paging = new PageDTO(page, size);
        return service.getInboundProductList(keyword, paging);
    }

    @GetMapping("/inbound/warehouse1")
    public WarehouseListRes getWarehouses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "") String keyword) {
        PageDTO paging = new PageDTO(page, size);
        return service.getWarehouseList(keyword, paging);
    }

    /* ===================== 주문승인 ===================== */

    @GetMapping("/approval-list")
    public List<SalesOrderVO> getApprovalList(SalesOrderVO vo) {
        return service.getApprovalList(vo);
    }

    @GetMapping("/approval/details")
    public Map<String, Object> getApprovalDetails(@RequestParam String orderId) {
        List<SalesOrderDetailVO> details = service.getApprovalDetails(orderId);
        return Map.of("data", details);
    }

    // @PostMapping("/approval/approve")
    // public Map<String, Object> approve(@RequestBody Map<String, Object> body) {
    // @SuppressWarnings("unchecked")
    // List<String> odetailIds = (List<String>) body.get("odetailIds");
    // int cnt = service.approveDetails(odetailIds);
    // return Map.of("retCode", cnt > 0 ? "success" : "fail", "count", cnt);
    // }

    @PostMapping("/approval/approve")
    public Map<String, Object> approve(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> odetailIds = (List<String>) body.get("odetailIds");
        int cnt = service.approveOrderWithProc(odetailIds, "승인");
        return Map.of("retCode", cnt > 0 ? "success" : "fail", "count", cnt);
    }

    // 기존 반려 프로시저 잠깐주석
    @PostMapping("/approval/reject")
    public Map<String, Object> reject(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> odetailIds = (List<String>) body.get("odetailIds");
        int cnt = service.approveOrderWithProc(odetailIds, "반려");
        return Map.of("retCode", cnt > 0 ? "success" : "fail", "count", cnt);
    }

    /* ===================== 반품승인 ===================== */

    @GetMapping("/return-list")
    public List<ReturnVO> getReturnList() {
        return service.getReturnList();
    }

    @GetMapping("/return-details")
    public Map<String, Object> getReturnDetails(@RequestParam String returnId) {
        List<ReturnDetailVO> details = service.getReturnDetails(returnId);
        return Map.of("data", details);
    }

    @PostMapping("/return/approve")
    public Map<String, Object> approveReturn(@RequestBody Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        List<String> ids = (List<String>) payload.get("ids");
        int cnt = service.approveReturnDetails(ids);
        return Map.of("retCode", cnt > 0 ? "success" : "fail", "count", cnt);
    }

    @PostMapping("/return/reject")
    public Map<String, Object> rejectReturn(@RequestBody Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        List<String> ids = (List<String>) payload.get("ids");
        String reason = (String) payload.get("reason");
        int cnt = service.rejectReturnDetails(ids, reason);
        return Map.of("retCode", cnt > 0 ? "success" : "fail", "count", cnt);
    }

    /* ===================== 출하지시 ===================== */

    // 출하지시 대상 주문 목록 조회
    @GetMapping("/ship/orders")
    public List<SalesOrderVO> getShippingOrders(SalesOrderVO vo) {
        return service.getShipOrders(vo);
    }

    // 출하지시 상세 조회
    @GetMapping("/ship/order-details")
    public Map<String, Object> getShipOrderDetails(@RequestParam String orderId) {
        List<SalesOrderDetailVO> details = service.getApprovalDetails(orderId);
        // 기존 주문상세 조회 재활용 가능
        return Map.of("data", details);
    }

    // 출하지시 등록 (여러 주문건)
    @PostMapping("/ship/orders")
    public ResponseEntity<Map<String, Object>> createShipOrders(@RequestBody List<SalesOrderVO> orders) {
        int cnt = service.createShipOrders(orders);
        return ResponseEntity.ok(Map.of("retCode", cnt > 0 ? "success" : "fail", "count", cnt));
    }

    // 단일 주문 부분출고 등록 (상세 단위)
    @PostMapping("/ship/order-details")
    public ResponseEntity<Map<String, Object>> createShipOrderDetails(@RequestBody List<SalesOrderDetailVO> details) {
        int cnt = service.createShipOrderDetails(details);
        return ResponseEntity.ok(Map.of("retCode", cnt > 0 ? "success" : "fail", "count", cnt));
    }

    /* ===================== 거래처원장 ===================== */

    // 거래처원장 목록 + 요약 (AccountLedger.vue)
    @GetMapping("/account-ledger")
    public ResponseEntity<Map<String, Object>> getAccountLedger(@ModelAttribute AccountLedgerSearchDTO condition) {

        Map<String, Object> result = service.getAccountLedger(condition);
        return ResponseEntity.ok(result);
    }

    // 대시보드
    @GetMapping("/account-dash")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> result = new HashMap<>();
        result.put("summary", service.getDashboardSummary());
        result.put("items", service.getDashboardList());
        return ResponseEntity.ok(result);
    }

    // ✅ 거래처원장 PDF 출력
    @GetMapping("/account-ledger/report")
    public void exportLedgerReport(HttpServletResponse response) throws Exception {
        // 1️⃣ 데이터 조회
        List<SalesOrderVO> voList = (List<SalesOrderVO>) service.getAccountLedger(new AccountLedgerSearchDTO())
                .get("items");

        // 2️⃣ VO → DTO 매핑
        List<WonjangReportDTO> reportList = voList.stream().map(vo -> {
            WonjangReportDTO dto = new WonjangReportDTO();
            dto.setCompanyName(vo.getCompanyName());
            dto.setTotalPrice(vo.getTotalSales());
            dto.setReturnPrice(vo.getTotalReturn());
            dto.setTotalPayment(vo.getTotalPayment());
            dto.setTotalAr(vo.getTotalAr());
            dto.setOrderCount(vo.getOrderCount());
            dto.setUnpaidCount(vo.getUnpaidCount());
            dto.setLastOrderDate(
                    vo.getLastOrderDate() != null
                            ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(vo.getLastOrderDate())
                            : "-");
            return dto;
        }).toList();

        // 3️⃣ Jasper 서비스 호출
        byte[] pdfBytes = jasperService.generateAccountLedgerReport(reportList);

        // 4️⃣ 응답으로 PDF 전송
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=wonjang_report.pdf");
        response.getOutputStream().write(pdfBytes);
    }

    /* ===================== 거래처원장 페이지 모달 ===================== */

    // ✅ 판매처 모달 조회 (원장페이지 전용)
    @GetMapping("/wonjang/modal-list")

    public Map<String, Object> getVendorList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.getVendorList(keyword, page, size);

    }

    /* ===================== 주문승인 페이지 모달 ===================== */

    @GetMapping("/approval-modal/vendor")
    public Map<String, Object> getApprovalVendorModal(
            @RequestParam(required = false) String condition,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.getApprovalVendorModal(condition, page, size);
    }

    // ✅ 주문번호 모달
    @GetMapping("/approval-modal/order")
    public Map<String, Object> getApprovalOrderModal(
            @RequestParam(required = false) String condition,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.getApprovalOrderModal(condition, page, size);
    }
}// end
