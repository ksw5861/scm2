package com.yedam.scm.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.yedam.scm.order.mapper.OrderMapper;
import com.yedam.scm.order.service.OrderService;
import com.yedam.scm.order.service.ReturnService;
import com.yedam.scm.order.service.PayService;
import com.yedam.scm.vo.SalesOrderVO;
import com.yedam.scm.vo.PaymentVO;
import com.yedam.scm.vo.ReturnVO;
import com.yedam.scm.vo.SalesOrderDetailVO;
import com.yedam.scm.vo.ProductVO;

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
    // Service & Mapper 의존성 주입
    // ==============================
    @Autowired
    private OrderService orderSvc;

    @Autowired
    private ReturnService returnSvc;

    @Autowired
    private PayService paySvc;

  
    // =================================================================
    // 1. 제품 목록 조회 (모달용)
    // -----------------------------------------------------------------
    // - 페이징 처리 포함
    // - 프론트 모달창에서 제품을 선택할 때 사용
    // =================================================================
    @GetMapping("/products")
    public Map<String, Object> getProducts(
            @RequestParam(required = false) String prodName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize) {

        int offset = (page - 1) * pageSize;
        int totalCount = orderSvc.getProductCount();
        List<ProductVO> items = orderSvc.getProductList(prodName, offset, pageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("items", items);

        return result;
    }

    // =================================================================
    // 2. 주문 등록
    // -----------------------------------------------------------------
    // - 판매처에서 신규 주문을 등록
    // - 주문 마스터 + 상세 동시 등록
    // =================================================================
    @PostMapping("/insertorder")
    public int insertOrder(@RequestBody SalesOrderVO orderVO) {
        return orderSvc.insertOrder(orderVO);
    }

    // =================================================================
    // 3. 주문 목록 조회
    // -----------------------------------------------------------------
    // - 조건 검색 가능
    //   (기간, 제품명, 상태, 주문번호)
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
    // -----------------------------------------------------------------
    // - 특정 주문 건의 상세 제품 내역 조회
    // =================================================================
    @GetMapping("/orderdetail")
    public List<SalesOrderDetailVO> getOrderDetailList(@RequestParam String orderId) {
        return orderSvc.getOrderDetailList(orderId);
    }

    // =================================================================
    // 4. 반품 등록
    // -----------------------------------------------------------------
    // - 다건 등록 지원
    // - 각 제품별로 반복 처리
    // =================================================================
    @PostMapping("/insertreturn")
    public int insertReturn(@RequestBody List<ReturnVO> returnList) {
        int total = 0;
        for (ReturnVO vo : returnList) {
            total += returnSvc.insertReturn(vo);
        }
        return total;
    }

    // =================================================================
    // 5. 반품 목록 조회
    // -----------------------------------------------------------------
    // - 반품 상태, 기간 조건으로 검색
    // =================================================================
    @GetMapping("/returnlist")
    public List<ReturnVO> getReturnList(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String returnStatus,
            @RequestParam(required = false) String prodName,
            @RequestParam(required = false) String returnNo
    ) {
        return returnSvc.getReturnList(startDate, endDate, returnStatus, prodName, returnNo);
    }

    // =================================================================
    // 6. 대금 결제 등록
    // -----------------------------------------------------------------
    // - 반품이나 주문에 대한 결제 처리
    // =================================================================
    @PostMapping("/insertpay")
    public int insertPay(@RequestBody PaymentVO payVO) {
        return paySvc.insertPay(payVO);
    }

    // =================================================================
    // 7. 납부 내역 조회
    // -----------------------------------------------------------------
    // - 결제 내역을 조건별 검색
    // =================================================================
    @GetMapping("/paylist")
    public List<PaymentVO> getPayList(
            @RequestParam(required = false) String payNo,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        return paySvc.getPayList(payNo, startDate, endDate);
    }

    // =================================================================
    // 8. 지점 대시보드
    // -----------------------------------------------------------------
    // - 지점별 총 매출, 주문 건수 등 현황 데이터 제공
    // =================================================================
    @GetMapping("/branchdash")
    public Object getBranchDashboard() {
        return orderSvc.getBranchDashData();
    }
}
