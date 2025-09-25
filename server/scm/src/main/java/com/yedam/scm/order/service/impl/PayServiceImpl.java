package com.yedam.scm.order.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.scm.order.mapper.PayMapper;
import com.yedam.scm.order.service.PayService;
import com.yedam.scm.vo.PaymentDetailVO;
import com.yedam.scm.vo.PaymentVO;
import com.yedam.scm.vo.SalesOrderVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayServiceImpl implements PayService {

    private final PayMapper payMapper;

    /**
     * 납부 등록
     * 1. PAYMENT 마스터 저장
     * 2. PAYMENT_DETAIL 저장
     * 3. 주문/반품 상태 COMPLETE 업데이트
     */
    @Transactional
    @Override
    public String insertPayment(PaymentVO paymentVO) {
        // 1. 납부 마스터 저장
        payMapper.insertPayment(paymentVO);

        String payId = paymentVO.getPayId();
        if (payId == null) {
            throw new RuntimeException("납부 등록 실패: PAY_ID가 생성되지 않았습니다.");
        }

        // 2. 납부 상세 저장
        if (paymentVO.getPaymentDetails() == null || paymentVO.getPaymentDetails().isEmpty()) {
            throw new RuntimeException("납부 등록 실패: 결제 상세 정보가 없습니다.");
        }

        for (PaymentDetailVO detail : paymentVO.getPaymentDetails()) {
            detail.setPayId(payId);
            payMapper.insertPaymentDetail(detail);
        }

        // 3. 주문/반품 상태 COMPLETE 업데이트
        List<String> orderId = paymentVO.getPaymentDetails().stream()
                .map(PaymentDetailVO::getOrderId)
                .distinct()
                .collect(Collectors.toList());

        if (!orderId.isEmpty()) {
            updateOrderStatusToComplete(orderId);  // ✅ 여기서 바로 호출
        }

        return payId;
    }

    @Override
    public Long insertPaymentDetail(PaymentDetailVO detailVO) {
        return payMapper.insertPaymentDetail(detailVO);
    }

    @Override
    public List<Map<String, Object>> selectPaymentList(String paymentNo, String startDate, String endDate) {
        return payMapper.selectPaymentList(paymentNo, startDate, endDate);
    }

    @Override
    public List<PaymentDetailVO> selectPaymentDetail(String paymentId) {
        return payMapper.selectPaymentDetail(paymentId);
    }

    @Override
    public List<SalesOrderVO> selectPendingOrders() {
        return payMapper.selectPendingOrders();
    }

    /**
     * 주문/반품 상태 COMPLETE 업데이트
     */
    @Override
    public void updateOrderStatusToComplete(List<String> orderId) {
        if (orderId != null && !orderId.isEmpty()) {
            payMapper.updateOrderStatusToComplete(orderId);
        }
    }
}
