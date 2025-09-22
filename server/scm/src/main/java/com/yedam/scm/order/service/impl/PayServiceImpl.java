package com.yedam.scm.order.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.scm.order.mapper.PayMapper;
import com.yedam.scm.order.service.PayService;
import com.yedam.scm.vo.PaymentDetailVO;
import com.yedam.scm.vo.PaymentVO;

@Service
public class PayServiceImpl implements PayService {

    @Autowired
    private PayMapper payMapper;

    @Override
    public int insertPay(PaymentVO payVO) {
        return payMapper.insertPayment(payVO);
    }

    @Override
    public int insertPayDetail(PaymentDetailVO detailVO) {
        return payMapper.insertPaymentDetail(detailVO);
    }

    @Override
    public List<PaymentVO> getPayList(String payNo, String startDate, String endDate) {
        return payMapper.getPayList(payNo, startDate, endDate);
    }

    @Override
    public List<PaymentDetailVO> getPayDetail(String payId) {
        return payMapper.getPayDetail(payId);
    }
}