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

    // =============================================================
    // 1. 납부 등록 (마스터)
    // =============================================================
    Long insertPayment(PaymentVO paymentVO);
    Long insertPaymentDetail(PaymentDetailVO detailVO);
    Long getCreditLimitByVendorId(@Param("vendorId") String vendorId);
    Long getAfterArBalanceByVendorId(@Param("vendorId") String vendorId);
    // =============================================================
    // 3. 납부 내역 조회
    // =============================================================
    List<Map<String, Object>> selectPaymentList(
        @Param("paymentNo") String paymentNo,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate,
        @Param("vendorId") String vendorId
    );

    // =============================================================
    // 4. 특정 납부 상세 조회
    // =============================================================
    List<PaymentDetailVO> selectPaymentDetail(@Param("paymentId") String paymentId);

    // =============================================================
    // 5. 결제 대기중인 주문 목록 조회
    // - 결제 가능 상태의 주문만 가져오기
    // =============================================================
    List<SalesOrderVO> selectPendingOrders(@Param("vendorId") String vendorId);

    // =============================================================
    // 6. 상태 업데이트
    // =============================================================
    // 주문 상태 'COMPLETE' 일괄 업데이트
    void updateOrderStatusToComplete(@Param("orderIdList") List<String> orderIdList);

    // 반품 상태 'COMPLETE' 일괄 업데이트
    void updateReturnStatusToComplete(@Param("returnIdList") List<String> returnIdList);

    // =============================================================
    // 7. 납부등록 페이지 - 상단 카드 데이터
    // =============================================================
    PaymentVO selectSummaryData();

    // =============================================================
    // 8. 납부 요약 리스트 조회
    // =============================================================
    List<PaymentVO> selectPaymentSummaryList();

}
