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
            paymentVO.setCreditLimit(null);
            // -----------------------------------------------------
            // 1️⃣ 여신한도 및 미수금 계산 (결제 전/후 기준)
            // -----------------------------------------------------
            String vendorId = paymentVO.getVendorId();

            Long creditLimit = payMapper.getCreditLimitByVendorId(vendorId); 
            if (creditLimit == null) creditLimit = 0L;

            Long afterAr = payMapper.getAfterArBalanceByVendorId(vendorId); // ★출고완료+배송완료 기준 미수금
            if (afterAr == null) afterAr = 0L;

            int payAmount = paymentVO.getPayAmount(); // 납부 금액

            // ✅ 계산 시작
            Long payPreAr = afterAr;                 // 결제 직전 미수금
            Long payAfterAr = afterAr - payAmount;   // 결제 후 미수금

            Long payPreLimit = creditLimit - payPreAr;   // 결제 전 여신 가능 금액
            Long payAfterLimit = creditLimit - payAfterAr; // 결제 후 여신 가능 금액

            // ✅ VO 저장
            paymentVO.setCreditLimit(creditLimit);
            paymentVO.setPayPreAr(payPreAr);
            paymentVO.setPayAfterAr(payAfterAr);
            paymentVO.setPayPreLimit(payPreLimit);
            paymentVO.setPayAfterLimit(payAfterLimit);

            // -----------------------------------------------------
            // 2️⃣ 납부 마스터 저장
            // -----------------------------------------------------
            Long masterResult = payMapper.insertPayment(paymentVO);
            if (masterResult == null || masterResult <= 0 || paymentVO.getPayId() == null) {
                throw new RuntimeException("Payment Master 저장 실패");
            }

            // -----------------------------------------------------
            // 3️⃣ 납부 상세 저장
            // -----------------------------------------------------
            List<PaymentDetailVO> detailList = paymentVO.getPaymentDetails();
            if (detailList == null || detailList.isEmpty()) {
                throw new RuntimeException("Payment Detail 데이터 없음");
            }

            for (PaymentDetailVO detail : detailList) {
                detail.setPayId(paymentVO.getPayId());
            }
            insertPaymentDetails(detailList);

            // -----------------------------------------------------
            // 4️⃣ 관련 주문/반품 상태 업데이트
            // -----------------------------------------------------
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
    public List<Map<String, Object>> selectPaymentList(String paymentNo, String startDate, String endDate, String vendorId) {
        return payMapper.selectPaymentList(paymentNo, startDate, endDate, vendorId);
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
    public List<SalesOrderVO> selectPendingOrders(String vendorId) {
        return payMapper.selectPendingOrders(vendorId);
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
