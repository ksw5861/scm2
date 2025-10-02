package com.yedam.scm.web;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.yedam.scm.order.service.BranchDashService;
import com.yedam.scm.order.service.IamportService;
import com.yedam.scm.order.service.OrderService;
import com.yedam.scm.order.service.ReturnService;
import com.yedam.scm.order.service.PayService;
import com.yedam.scm.vo.SalesOrderVO;
import com.yedam.scm.vo.PaymentVO;
import com.yedam.scm.vo.ReturnVO;
import com.yedam.scm.vo.SalesOrderDetailVO;
import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.ReturnDetailVO;

/**
 * EgController
 * ============================================================
 * 판매처 관련 전체 API Controller
 * 주문, 반품, 결제 등 주요 비즈니스 로직의 API 진입점
 * ============================================================
 */
@RestController
@RequestMapping // TODO: prefix 지정 필요 (예: "/api")
public class EgController {

    @Autowired
    private OrderService orderSvc;

    @Autowired
    private ReturnService returnSvc;

    @Autowired
    private PayService paymentSvc;

    @Autowired
    private IamportService iamportService;

    @Autowired
    private BranchDashService branchDashService;

    // =================================================================
    // 1. 제품 목록 조회 (모달용)
    // =================================================================
    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> getProducts(
            @RequestParam(required = false) String prodName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize) {

        Map<String, Object> response = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;
            int totalCount = orderSvc.getProductCount();

            Map<String, Object> params = new HashMap<>();
            params.put("prodName", prodName);
            params.put("offset", offset);
            params.put("pageSize", pageSize);
            params.put("status", "Y"); // 활성화된 제품만 조회
            List<ProductVO> items = orderSvc.getProductList(params);

            response.put("status", "success");
            response.put("count", totalCount);
            response.put("items", items);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "제품 목록 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // =================================================================
    // 2. 주문 등록
    // =================================================================
    @PostMapping("/insertorder")
    public ResponseEntity<Map<String, Object>> insertOrder(@RequestBody SalesOrderVO orderVO) {
        Map<String, Object> response = new HashMap<>();
        // TODO: logger 사용 권장
        System.out.println("Received OrderVO: " + orderVO);

        try {
            boolean success = orderSvc.insertOrder(orderVO);
            if (success) {
                response.put("status", "success");
                response.put("orderId", orderVO.getOrderId());
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "주문 등록에 실패했습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "서버 오류로 주문 등록에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // =================================================================
    // 3. 주문 목록 조회
    // =================================================================
    @GetMapping("/orderlist")
    public ResponseEntity<Map<String, Object>> getOrderListForView(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String prodName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderId) {

        Map<String, Object> response = new HashMap<>();
        try {
            List<SalesOrderVO> orders = orderSvc.getOrderListForView(startDate, endDate, prodName, status, orderId);

            response.put("status", "success");
            response.put("count", orders.size());
            response.put("orders", orders);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "주문 목록 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // =================================================================
    // 3-1. 주문 상세 조회
    // =================================================================
    @GetMapping("/orders/{orderId}/details")
    public ResponseEntity<Map<String, Object>> getOrderDetailList(@PathVariable String orderId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<SalesOrderDetailVO> details = orderSvc.getOrderDetailList(orderId);

            response.put("status", "success");
            response.put("count", details.size());
            response.put("details", details);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "주문 상세 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // =================================================================
    // 3-2. 주문 상태 변경 (예: 배송중 -> 배송완료)
    // =================================================================
    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable String orderId,
            @RequestBody Map<String, String> body) {

        String newStatus = body.get("status");
        boolean success = orderSvc.updateOrderStatus(orderId, newStatus);

        if (success) {
            return ResponseEntity.ok(Map.of("status", "success"));
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "fail", "message", "업데이트 실패"));
        }
    }



    // =================================================================
    // 3-2. 주문 취소 (주문마스터의 상태가 대기에 한해서)
    // =================================================================
    @DeleteMapping("/orders/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable String orderId) {
        int result = orderSvc.deleteOrderWithDetails(orderId);
        if(result > 0) {
            return ResponseEntity.ok(Map.of("status", "success"));
        } else {
            return ResponseEntity.ok(Map.of("status", "fail", "message", "삭제할 주문이 없습니다."));
        }
    }




    // =================================================================
    // 4. 반품 등록
    // =================================================================
    @PostMapping("/insertreturn")
    public ResponseEntity<Map<String, Object>> insertReturn(@RequestBody ReturnVO returnVO) {
        Map<String, Object> response = new HashMap<>();
        try {
            int result = returnSvc.insertReturn(returnVO);

            if (result > 0) {
                response.put("status", "success");
                response.put("returnId", returnVO.getReturnId());
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "반품 등록에 실패했습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "서버 오류로 반품 등록에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // =================================================================
    // 5. 반품 목록 조회
    // =================================================================
    @GetMapping("/returnlist")
    public ResponseEntity<Map<String, Object>> getReturnList(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String returnStatus,
           
            @RequestParam(required = false) String returnId) {

        Map<String, Object> response = new HashMap<>();
        try {
            List<ReturnVO> list = returnSvc.getReturnList(startDate, endDate, returnStatus, returnId);

            response.put("status", "success");
            response.put("count", list.size());
            response.put("list", list);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "반품 목록 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // =================================================================
    // 5-1. 반품 상세 조회
    // =================================================================
    @GetMapping("/returns/{returnId}/details")
    public ResponseEntity<Map<String, Object>> getReturnDetail(@PathVariable String returnId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ReturnDetailVO> details = returnSvc.getReturnDetailList(returnId);

            response.put("status", "success");
            response.put("count", details.size());
            response.put("details", details);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "반품 상세 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // =================================================================
    // 5-2. 반품 상태 변경 (대기 -> 배송중 -> 배송완료)
    // =================================================================
    @PutMapping("/returns/{returnId}/status")
    public ResponseEntity<?> updateReturnStatus(
            @PathVariable String returnId,
            @RequestBody Map<String, String> body) {

        String newStatus = body.get("status");
        boolean success = returnSvc.updateReturnStatus(returnId, newStatus);

        if (success) {
            return ResponseEntity.ok(Map.of("status", "success"));
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "fail", "message", "업데이트 실패"));
        }
    }

    // =================================================================
    // 6. 대금 결제 등록 (보류/미사용)
    // =================================================================
    // @PostMapping("/payments")
    // public ResponseEntity<Map<String, Object>> insertPayment(@RequestBody PaymentVO paymentVO) { ... }

    // =================================================================
    // 7. 납부 내역 조회
    // =================================================================
    @GetMapping("/payments")
    public ResponseEntity<Map<String, Object>> selectPaymentList(
            @RequestParam(required = false) String paymentNo,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> list = paymentSvc.selectPaymentList(paymentNo, startDate, endDate);

            response.put("status", "success");
            response.put("count", list.size());
            response.put("list", list);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "납부 내역 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // =================================================================
    // 8. 결제 대기중 주문건 목록
    // =================================================================
    @GetMapping("/pendingorders")
    public ResponseEntity<List<SalesOrderVO>> selectPendingOrders() {
        try {
            List<SalesOrderVO> list = paymentSvc.selectPendingOrders();
            return ResponseEntity.ok(list); // 배열 그대로 리턴
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // =================================================================
    // 10. 결제 요약 데이터
    // =================================================================
    @GetMapping("/paymentsummary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        Map<String, Object> response = new HashMap<>();
        try {
            PaymentVO summary = paymentSvc.getSummaryData();

            response.put("status", "success");
            response.put("data", summary);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "결제 요약 데이터 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // =================================================================
    // 11. 결제 요약 리스트
    // =================================================================
    @GetMapping("/paymentsummarylist")
    public ResponseEntity<Map<String, Object>> getPaymentSummaryList() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<PaymentVO> list = paymentSvc.getPaymentSummaryList();

            response.put("status", "success");
            response.put("count", list.size());
            response.put("list", list);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "결제 요약 리스트 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // =================================================================
    // 12. 반품 가능 제품 조회 (모달 대신 페이지 메인)
    // =================================================================
    @GetMapping("/returnableproducts")
    public ResponseEntity<Map<String, Object>> getReturnableOrders(
            @RequestParam String vendorId,
            @RequestParam(required = false) String prodName) {

        Map<String, Object> response = new HashMap<>();
        try {
            List<ReturnVO> list = returnSvc.getReturnableOrders(vendorId, prodName);

            response.put("status", "success");
            response.put("count", list.size());
            response.put("items", list);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "반품 가능 제품 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // =================================================================
    // 12-1. 반품 가능 제품 상세 조회
    // =================================================================
    @GetMapping("/returnableorder/{orderId}/details")
    public ResponseEntity<Map<String, Object>> getReturnableOrderDetails(@PathVariable String orderId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ReturnDetailVO> details = returnSvc.getReturnableOrderDetails(orderId);

            response.put("status", "success");
            response.put("count", details.size());
            response.put("details", details);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "반품 가능 주문 상세 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // =================================================================
    // 13. 아임포트 결제 검증 및 저장
    // =================================================================
    @PostMapping("/verify-payment")
    public ResponseEntity<Map<String, Object>> verifyPayment(@RequestBody PaymentVO req) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 1. 아임포트 서버 검증
            Map<String, Object> verifyResult = iamportService.verifyPayment(req.getImpUid());
            if (verifyResult == null || verifyResult.get("response") == null) {
                response.put("status", "error");
                response.put("message", "아임포트 서버 응답이 없습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Map<String, Object> paymentData = (Map<String, Object>) verifyResult.get("response");
            int actualAmount = ((Number) paymentData.get("amount")).intValue();
            String status = (String) paymentData.get("status");

            // 결제 금액 검증
            if (actualAmount != req.getTotalAmount()) {
                response.put("status", "error");
                response.put("message", "결제 금액 불일치");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // 결제 상태 확인
            if (!"paid".equalsIgnoreCase(status)) {
                response.put("status", "error");
                response.put("message", "결제가 완료되지 않았습니다. 상태: " + status);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // 2. DB 저장
            PaymentVO vo = new PaymentVO();
            vo.setPayAmount(req.getTotalAmount());
            vo.setPayType(req.getPayType());
            vo.setVendorId(req.getVendorId());
            vo.setPayRmk(req.getMemo());
            vo.setPayDate(new Date());
            if (req.getPaymentDetails() != null && !req.getPaymentDetails().isEmpty()) {
                vo.setPaymentDetails(req.getPaymentDetails());
            }

            // 3. 서비스 호출
            String result = paymentSvc.insertPayment(vo);
            if (!"SUCCESS".equalsIgnoreCase(result)) {
                response.put("status", "error");
                response.put("message", "결제 정보 저장 실패");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            response.put("status", "success");
            response.put("message", "결제가 정상 처리되었습니다.");
            response.put("paymentId", vo.getPayId());
            return ResponseEntity.ok(response);
         } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "서버 오류: " + e.getMessage());
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }

    // =================================================================
    // 대시보드 - 매출 추이 그래프
    // =================================================================
    @GetMapping("/salestrend")
    public List<Map<String, Object>> getSalesTrend(
            @RequestParam String vendorId,
            @RequestParam String range) {
        return branchDashService.getSalesTrend(vendorId, range);
    }

}