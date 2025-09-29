package com.yedam.scm.order.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private final PayMapper payMapper;

    /**
     * 납부 등록
     * 1. PAYMENT 마스터 저장
     * 2. PAYMENT_DETAIL 다건 저장
     * 3. 주문 / 반품 상태 COMPLETE 업데이트
     */
    @Transactional
    @Override
    public String insertPayment(PaymentVO paymentVO) {
        try {
            // ========== 1. 결제 마스터 저장 ==========
            Long masterResult = payMapper.insertPayment(paymentVO);
            if (masterResult == null || masterResult <= 0 || paymentVO.getPayId() == null) {
                throw new RuntimeException("Payment Master 저장 실패");
            }

            // ========== 2. 결제 상세 저장 ==========
            List<PaymentDetailVO> detailList = paymentVO.getPaymentDetails();
            if (detailList == null || detailList.isEmpty()) {
                throw new RuntimeException("Payment Detail 데이터가 존재하지 않습니다.");
            }

                for (PaymentDetailVO detail : detailList) {
                detail.setPayId(paymentVO.getPayId()); // 마스터에서 생성된 payId 세팅
                }
                insertPaymentDetails(detailList);

            // ========== 3. 상태 업데이트 ==========
            List<String> orderIds = new ArrayList<>();
            List<String> returnIds = new ArrayList<>();

            for (PaymentDetailVO detail : detailList) {
                if ("ORDER".equalsIgnoreCase(detail.getDataType())) {
                    orderIds.add(detail.getOrderId());
                } else if ("RETURN".equalsIgnoreCase(detail.getDataType())) {
                    returnIds.add(detail.getReturnId());
                }
            }

            if (!orderIds.isEmpty()) {
                payMapper.updateOrderStatusToComplete(orderIds);
            }
            if (!returnIds.isEmpty()) {
                payMapper.updateReturnStatusToComplete(returnIds);
            }

            return "SUCCESS";

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("납부 등록 처리 중 오류 발생", e); // 트랜잭션 롤백
        }
    }

    /**
     * 납부 상세 다건 저장
     */
    @Override
    public Long insertPaymentDetails(List<PaymentDetailVO> detailList) {
        long totalInserted = 0;
        for (PaymentDetailVO detail : detailList) {
            totalInserted += payMapper.insertPaymentDetail(detail);
        }
        return totalInserted;
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
    public void updateOrderStatusToComplete(List<String> orderIds) {
        if (orderIds != null && !orderIds.isEmpty()) {
            payMapper.updateOrderStatusToComplete(orderIds);
        }
    }

    @Override
    public void updateReturnStatusToComplete(List<String> returnIds) {
        if (returnIds != null && !returnIds.isEmpty()) {
            payMapper.updateReturnStatusToComplete(returnIds);
        }
    }

    @Override
    public PaymentVO getSummaryData() {
        return payMapper.selectSummaryData();
    }

    @Override
    public List<PaymentVO> getPaymentSummaryList() {
        return payMapper.selectPaymentSummaryList();
    }
}
