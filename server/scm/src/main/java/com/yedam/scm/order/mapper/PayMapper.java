package com.yedam.scm.order.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.yedam.scm.vo.PaymentDetailVO;
import com.yedam.scm.vo.PaymentVO;
import com.yedam.scm.vo.SalesOrderVO;

@Mapper
public interface PayMapper {

     // 결제 등록 (마스터)
    Long insertPayment(PaymentVO paymentVO);

    // 결제 상세 등록
    Long insertPaymentDetail(PaymentDetailVO detailVO);

    // 결제 내역 조회
    List<Map<String, Object>> selectPaymentList(String paymentNo, String startDate, String endDate);

    // 특정 결제 상세 조회
    List<PaymentDetailVO> selectPaymentDetail(String paymentId);

    
    //결제대기중인 주문목록
    List<SalesOrderVO> selectPendingPaymentOrders();


    
}