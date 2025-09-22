package com.yedam.scm.order.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.yedam.scm.vo.PaymentDetailVO;
import com.yedam.scm.vo.PaymentVO;

@Mapper
public interface PayMapper {

    // 결제 등록
    int insertPayment(PaymentVO payVO);

    // 결제 상세 등록
    int insertPaymentDetail(PaymentDetailVO detailVO);

    // 납부 내역 조회
    List<PaymentVO> getPayList(String payNo, String startDate, String endDate);

    // 특정 결제 상세 조회
    List<PaymentDetailVO> getPayDetail(String payId);
}