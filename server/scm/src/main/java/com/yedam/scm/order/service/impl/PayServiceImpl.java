package com.yedam.scm.order.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    // =============================================================
    // 납부 등록 (마스터 + 상세 + 상태 변경)
    // =============================================================
    @Transactional
    @Override
    public String insertPayment(PaymentVO paymentVO) {
        try {
            Long masterResult = payMapper.insertPayment(paymentVO);
            if (masterResult == null || masterResult <= 0 || paymentVO.getPayId() == null) {
                throw new RuntimeException("Payment Master 저장 실패");
            }

            List<PaymentDetailVO> detailList = paymentVO.getPaymentDetails();
            if (detailList == null || detailList.isEmpty()) {
                throw new RuntimeException("Payment Detail 데이터 없음");
            }

            for (PaymentDetailVO detail : detailList) {
                detail.setPayId(paymentVO.getPayId());
            }
            insertPaymentDetails(detailList);

            List<String> orderIds = new ArrayList<>();
            List<String> returnIds = new ArrayList<>();

            for (PaymentDetailVO detail : detailList) {
                if ("ORDER".equalsIgnoreCase(detail.getDataType())) {
                    orderIds.add(detail.getOrderId());
                } else if ("RETURN".equalsIgnoreCase(detail.getDataType())) {
                    returnIds.add(detail.getReturnId());
                }
            }

            if (!orderIds.isEmpty()) payMapper.updateOrderStatusToComplete(orderIds);
            if (!returnIds.isEmpty()) payMapper.updateReturnStatusToComplete(returnIds);

            return "SUCCESS";

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("납부 등록 처리 중 오류 발생", e);
        }
    }

    // =============================================================
    // 납부 상세 다건 저장
    // =============================================================
    @Override
    public Long insertPaymentDetails(List<PaymentDetailVO> detailList) {
        long totalInserted = 0;
        for (PaymentDetailVO detail : detailList) {
            totalInserted += payMapper.insertPaymentDetail(detail);
        }
        return totalInserted;
    }

    // =============================================================
    // 납부 내역 조회
    // =============================================================
    @Override
    public List<Map<String, Object>> selectPaymentList(String paymentNo, String startDate, String endDate) {
        return payMapper.selectPaymentList(paymentNo, startDate, endDate);
    }

    // =============================================================
    // 납부 상세 조회
    // =============================================================
    @Override
    public List<PaymentDetailVO> selectPaymentDetail(String paymentId) {
        return payMapper.selectPaymentDetail(paymentId);
    }

    // =============================================================
    // 결제 대기중인 주문 조회
    // =============================================================
    @Override
    public List<SalesOrderVO> selectPendingOrders() {
        return payMapper.selectPendingOrders();
    }

    // =============================================================
    // 주문 상태 COMPLETE 업데이트
    // =============================================================
    @Override
    public void updateOrderStatusToComplete(List<String> orderIds) {
        if (orderIds != null && !orderIds.isEmpty()) {
            payMapper.updateOrderStatusToComplete(orderIds);
        }
    }

    // =============================================================
    // 반품 상태 COMPLETE 업데이트
    // =============================================================
    @Override
    public void updateReturnStatusToComplete(List<String> returnIds) {
        if (returnIds != null && !returnIds.isEmpty()) {
            payMapper.updateReturnStatusToComplete(returnIds);
        }
    }

    // =============================================================
    // 납부등록 상단 카드 데이터 조회
    // =============================================================
    @Override
    public PaymentVO getSummaryData() {
        return payMapper.selectSummaryData();
    }

    // =============================================================
    // 납부 요약 리스트 조회
    // =============================================================
    @Override
    public List<PaymentVO> getPaymentSummaryList() {
        return payMapper.selectPaymentSummaryList();
    }
}
