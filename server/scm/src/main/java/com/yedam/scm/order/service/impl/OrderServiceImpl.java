package com.yedam.scm.order.service.impl;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.scm.order.mapper.OrderMapper;
import com.yedam.scm.order.service.OrderService;
import com.yedam.scm.vo.SalesOrderDetailVO;
import com.yedam.scm.vo.SalesOrderVO;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public int insertOrder(SalesOrderVO orderVO) {
        return orderMapper.insertOrder(orderVO);
    }

    @Override
    public int insertOrderDetail(SalesOrderDetailVO detailVO) {
        return orderMapper.insertOrderDetail(detailVO);
    }

   

    @Override
    public List<SalesOrderDetailVO> getOrderDetails(String orderId) {
        return orderMapper.getOrderDetails(orderId);
    }


    @Override
    public List<SalesOrderVO> getOrderListForView(String startDate, String endDate, String prodName,  String status, String orderId) {
        return orderMapper.getOrderListForView(startDate, endDate, prodName, status, orderId);
    }

    @Override
    public Object getBranchDashData() {
        return orderMapper.getBranchDashData();
    }

    @Override
    public List<SalesOrderVO> getOrderDetailList(String orderId) {
    return orderMapper.getOrderDetailList(orderId);
}


    
}