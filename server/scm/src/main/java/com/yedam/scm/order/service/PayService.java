package com.yedam.scm.order.service;

import java.util.List;
import java.util.Map;

import com.yedam.scm.vo.PaymentDetailVO;

import com.yedam.scm.vo.PaymentVO;
import com.yedam.scm.vo.SalesOrderVO;

public interface PayService {

    /** 
     * 납부 등록 (마스터 + 상세 + 주문/반품 상태 변경까지 트랜잭션)
     */
    String insertPayment(PaymentVO paymentVO);

    /** 
     * 납부 상세 저장 (다건)
     * - for문 or MyBatis batch insert
     */
    Long insertPaymentDetails(List<PaymentDetailVO> detailList);

    /** 납부 내역 조회 */
    List<Map<String, Object>> selectPaymentList(String paymentNo, String startDate, String endDate);

    /** 특정 납부 상세 조회 */
    List<PaymentDetailVO> selectPaymentDetail(String paymentId);

    /** 결제 대기중인 주문 목록 조회 */
    List<SalesOrderVO> selectPendingOrders();

    /** 주문 상태 COMPLETE 업데이트 */
    void updateOrderStatusToComplete(List<String> orderIds);

    /** 반품 상태 COMPLETE 업데이트 */
    void updateReturnStatusToComplete(List<String> returnIds);

    /** 납부등록 페이지 - 상단 카드 데이터 조회 */
    PaymentVO getSummaryData();

    /** 납부 요약 리스트 조회 */
    List<PaymentVO> getPaymentSummaryList();


}
