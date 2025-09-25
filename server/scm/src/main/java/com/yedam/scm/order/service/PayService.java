package com.yedam.scm.order.service;

import java.util.List;
import java.util.Map;

import com.yedam.scm.vo.PaymentDetailVO;
import com.yedam.scm.vo.PaymentVO;
import com.yedam.scm.vo.SalesOrderVO;

public interface PayService {
    Long insertPayment(PaymentVO paymentVO); 
    Long insertPaymentDetail(PaymentDetailVO detailVO); 
    List<Map<String, Object>> selectPaymentList(String paymentNo, String startDate, String endDate); 
    List<PaymentDetailVO> selectPaymentDetail(String paymentId); 
    
    /** 결제대기중인 주문건 목록 */
    List<SalesOrderVO> selectPendingOrders();
}
