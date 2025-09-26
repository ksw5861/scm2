package com.yedam.scm.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
public class EgController {

    // ==============================
    // Service 의존성 주입
    // ==============================
    @Autowired
    private OrderService orderSvc;

    @Autowired
    private ReturnService returnSvc;

    @Autowired
    private PayService paymentSvc;

    // =================================================================
    // 1. 제품 목록 조회 (모달용)
    // =================================================================
    @GetMapping("/products")
    public Map<String, Object> getProducts(
            @RequestParam(required = false) String prodName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize) {

        int offset = (page - 1) * pageSize;
        int totalCount = orderSvc.getProductCount();

        // ✅ 제품 목록은 반드시 PROD_UNIT_PRICE 컬럼 사용
        List<ProductVO> items = orderSvc.getProductList(prodName, offset, pageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("items", items);

        return result;
    }

    // =================================================================
    // 2. 주문 등록
    // =================================================================
    @PostMapping("/insertorder")
    public int insertOrder(@RequestBody SalesOrderVO orderVO) {
        return orderSvc.insertOrder(orderVO);
    }

    // =================================================================
    // 3. 주문 목록 조회
    // =================================================================
    @GetMapping("/orderlist")
    public List<SalesOrderVO> getOrderListForView(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String prodName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderId
    ) {
        return orderSvc.getOrderListForView(startDate, endDate, prodName, status, orderId);
    }

    // =================================================================
    // 3-1. 주문 상세 조회
    // =================================================================
    @GetMapping("/orderdetail")
    public List<SalesOrderDetailVO> getOrderDetailList(@RequestParam String orderId) {
        return orderSvc.getOrderDetailList(orderId); // ✅ 내부적으로 prodUnitPrice 사용
    }

    // =================================================================
    // 4. 반품 등록
    // =================================================================
    @PostMapping("/insertreturn")
    public int insertReturn(@RequestBody ReturnVO returnVO) {
        // 1) 반품 마스터 + 상세를 한 번에 INSERT
        // returnVO 내부에 List<ReturnDetailVO> details 포함
        return returnSvc.createReturnSlip(returnVO);
    }

    // =================================================================
    // 5. 반품 목록 조회
    // =================================================================
    @GetMapping("/returnlist")
    public List<ReturnVO> getReturnList(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String returnStatus,
            @RequestParam(required = false) String prodName,
            @RequestParam(required = false) String returnId
    ) {
        return returnSvc.getReturnList(startDate, endDate, returnStatus, prodName, returnId);
    }

    // =================================================================
    // 5-1. 반품 상세 조회
    // =================================================================
    @GetMapping("/returndetail")
    public List<ReturnDetailVO> getReturnDetail(@RequestParam String returnId) {
        return returnSvc.getReturnDetailList(returnId);  
    }

    // =================================================================
    // 6. 대금 결제 등록
    // =================================================================
    @PostMapping("/insertpayment")
    public String insertPayment(@RequestBody PaymentVO paymentVO) {
        // ✅ 납부 마스터 + 상세 Insert 및 주문 상태 업데이트
        return paymentSvc.insertPayment(paymentVO);
    }

    // =================================================================
    // 7. 납부 내역 조회
    // =================================================================
    @GetMapping("/paymentlist")
    public List<Map<String, Object>> selectPaymentList(
            @RequestParam(required = false) String paymentNo,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        return paymentSvc.selectPaymentList(paymentNo, startDate, endDate);
    }


    // =================================================================
    // 8. 결제 대기중인 주문건 목록
    // =================================================================
    @GetMapping("/pendingorders")
    public List<SalesOrderVO> selectPendingOrders() {
        /*
         * 결제 대상 조건
         * ----------------------------------------------
         * 1) 주문건: pay_status = '대기'
         * 2) 반품건: return_status = '승인' (미수금 차감 전용, 목록에는 미출력)
         * 
         * SQL 레벨에서 조건 처리:
         *  - ORDER: 결제대기 건만 SELECT
         *  - RETURN: 승인된 반품건만 SELECT
         * 
         * 프론트에서는 orders 리스트로 결제대기 주문건만 출력하고,
         * 미수금 계산 시 승인된 반품건 금액은 음수로 합산되어 자동 차감됨.
         */
        return paymentSvc.selectPendingOrders();
    }


    @GetMapping("/branchdash")
    public Object getBranchDashboard() {
    return orderSvc.getBranchDashData();
    }




}
