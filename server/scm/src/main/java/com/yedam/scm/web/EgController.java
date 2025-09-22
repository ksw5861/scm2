package com.yedam.scm.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.yedam.scm.order.service.OrderService;
import com.yedam.scm.order.service.ReturnService;
import com.yedam.scm.order.service.PayService;

import com.yedam.scm.vo.SalesOrderVO;
import com.yedam.scm.vo.PaymentVO;
import com.yedam.scm.vo.ReturnVO;
import com.yedam.scm.vo.SalesOrderDetailVO;


/**
 * EgController
 * 판매처 관련 전체 API Controller
 */
@RestController
@RequestMapping
public class EgController {

    @Autowired
    private OrderService orderSvc;

    @Autowired
    private ReturnService returnSvc;

    @Autowired
    private PayService paySvc;

    // ==========================
    // 1. 주문 등록 (판매처)
    // ==========================
    @PostMapping("/insertorder")
    public int insertOrder(@RequestBody SalesOrderVO orderVO) {
        return orderSvc.insertOrder(orderVO);
    }
    

    // ==========================
    // 2. 주문 목록 조회
    // ==========================
    @GetMapping("/orderlist")
    public List<SalesOrderVO> getOrderListForView(
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate,
        @RequestParam(required = false) String prodName,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String orderId) {

    return orderSvc.getOrderListForView(startDate, endDate, prodName, status, orderId);
    }
    
    // ==========================
    // 2-1. 주문 상세 조회
    // ==========================
    @GetMapping("/orderdetail")
    public List<SalesOrderVO> getOrderDetailList(@RequestParam String orderId) {
    return orderSvc.getOrderDetailList(orderId);
    }


    // 3. 반품 등록
    // ==========================
    @PostMapping("/insertreturn")
    public int insertReturn(@RequestBody ReturnVO returnVO) {
        return returnSvc.insertReturn(returnVO);
    }

    // ==========================
    // 4. 반품 목록 조회
    // ==========================
    @GetMapping("/returnlist")
    public List<ReturnVO> getReturnList(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status) {

        return returnSvc.getReturnList(startDate, endDate, status);
    }

    // ==========================
    // 5. 대금 결제 등록
    // ==========================
    @PostMapping("/insertpay")
    public int insertPay(@RequestBody PaymentVO payVO) {
        return paySvc.insertPay(payVO);
    }

    // ==========================
    // 6. 납부 내역 조회
    // ==========================
    @GetMapping("/paylist")
    public List<PaymentVO> getPayList(
            @RequestParam(required = false) String payNo,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        return paySvc.getPayList(payNo, startDate, endDate);
    }

    // ==========================
    // 7. 지점 대시보드
    // ==========================
    @GetMapping("/branchdash")
    public Object getBranchDashboard() {
        return orderSvc.getBranchDashData();
    }
}
