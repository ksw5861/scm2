package com.yedam.scm.order.service.impl;

import java.util.ArrayList;
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
    // 1. 결제 마스터 저장
    payMapper.insertPayment(paymentVO);

    // 2. 결제 상세 저장
    for (PaymentDetailVO detail : paymentVO.getPaymentDetails()) {
        detail.setPayId(paymentVO.getPayId());
        payMapper.insertPaymentDetail(detail);
    }

    // 3. 납부한 주문건 상태 '완료'로 변경
    List<String> orderIds = new ArrayList<>();
    for (PaymentDetailVO detail : paymentVO.getPaymentDetails()) {
        if ("ORDER".equals(detail.getDataType())) {
            orderIds.add(detail.getOrderId());
        }
    }

    if (!orderIds.isEmpty()) {
        payMapper.updateOrderStatusToComplete(orderIds);
    }

    return "OK";
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

    @Override
    public void updateOrderStatusToComplete(List<String> orderId) {
        if (orderId != null && !orderId.isEmpty()) {
            payMapper.updateOrderStatusToComplete(orderId);
        }
    }

    @Override
    public void updateReturnStatusToComplete(List<String> returnId) {
        if (returnId != null && !returnId.isEmpty()) {
            payMapper.updateReturnStatusToComplete(returnId);
        }
    }







}
