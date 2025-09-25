package com.yedam.scm.order.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
    List<Map<String, Object>> selectPaymentList(@Param("paymentNo") String paymentNo,
                                                @Param("startDate") String startDate,
                                                @Param("endDate") String endDate);

    // 특정 결제 상세 조회
    List<PaymentDetailVO> selectPaymentDetail(@Param("paymentId") String paymentId);

    // 결제대기중인 주문 + 반품 목록 조회
    List<SalesOrderVO> selectPendingOrders();

    // 주문/반품 상태 COMPLETE 일괄 업데이트
    void updateOrderStatusToComplete(@Param("orderId") List<String> orderId);
}
