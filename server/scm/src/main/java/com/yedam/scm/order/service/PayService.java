package com.yedam.scm.order.service;

import java.util.List;
import java.util.Map;

import com.yedam.scm.vo.PaymentDetailVO;
import com.yedam.scm.vo.PaymentVO;
import com.yedam.scm.vo.SalesOrderVO;

public interface PayService {
    
    /** 납부 등록 (마스터 + 상세 저장 + 상태 업데이트까지) */
    String insertPayment(PaymentVO paymentVO); 

    /** 납부 상세 저장 (단건) */
    Long insertPaymentDetail(PaymentDetailVO detailVO); 

    /** 납부 내역 조회 */
    List<Map<String, Object>> selectPaymentList(String paymentNo, String startDate, String endDate); 

    /** 특정 납부 상세 조회 */
    List<PaymentDetailVO> selectPaymentDetail(String paymentId); 
    
    /** 결제대기중인 주문건 + 반품 목록 조회 */
    List<SalesOrderVO> selectPendingOrders();

    /** 주문 상태 COMPLETE 업데이트 (여러건) */
    void updateOrderStatusToComplete(List<String> orderId);

    /** 반품 상태 COMPLETE 업데이트 (단일건) */
    void updateReturnStatusToComplete(List<String> returnId);

    /** 납부등록페이지 - 상단 카드들 */
    PaymentVO getSummaryData();

    /** 납부내역 */
    List<PaymentVO> getPaymentSummaryList();

}
