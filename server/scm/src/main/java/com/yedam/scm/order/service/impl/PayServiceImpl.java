package com.yedam.scm.order.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.scm.order.mapper.PayMapper;
import com.yedam.scm.order.service.PayService;
import com.yedam.scm.vo.PaymentDetailVO;
import com.yedam.scm.vo.PaymentVO;
import com.yedam.scm.vo.SalesOrderVO;

@Service
public class PayServiceImpl implements PayService {

    @Autowired
    private PayMapper paymentMapper; 

        @Override
        public Long insertPayment(PaymentVO paymentVO) { 
            return paymentMapper.insertPayment(paymentVO);
        }

        @Override
        public Long insertPaymentDetail(PaymentDetailVO detailVO) {
            return paymentMapper.insertPaymentDetail(detailVO);
        }

        @Override
        public List<Map<String, Object>> selectPaymentList(String paymentNo, String startDate, String endDate) { 
            return paymentMapper.selectPaymentList(paymentNo, startDate, endDate);
        }

        @Override
        public List<PaymentDetailVO> selectPaymentDetail(String paymentId) { 
            return paymentMapper.selectPaymentDetail(paymentId);
        }

        //결제 대기중인 주문건 목록
        @Override
        public List<SalesOrderVO> selectPendingOrders() {
            return paymentMapper.selectPendingPaymentOrders();
        }
}
