package com.yedam.scm.order.service;

import java.util.List;

import com.yedam.scm.vo.PaymentDetailVO;
import com.yedam.scm.vo.PaymentVO;

public interface PayService {
    int insertPay(PaymentVO payVO);
    int insertPayDetail(PaymentDetailVO detailVO);
    List<PaymentVO> getPayList(String payNo, String startDate, String endDate);
    List<PaymentDetailVO> getPayDetail(String payId);
}