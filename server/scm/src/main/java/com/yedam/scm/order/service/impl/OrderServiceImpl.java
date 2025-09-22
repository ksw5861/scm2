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
    public List<SalesOrderVO> getOrderList(String startDate, String endDate, String status) {
        return orderMapper.getOrderList(startDate, endDate, status);
    }

    @Override
    public List<SalesOrderDetailVO> getOrderDetails(String orderId) {
        return orderMapper.getOrderDetails(orderId);
    }


    @Override
    public List<SalesOrderVO> getOrderListForView(String startDate, String endDate, String status) {
        return orderMapper.getOrderListForView(startDate, endDate, status);
    }

    @Override
    public Object getBranchDashData() {
        return orderMapper.getBranchDashData();
    }


    
}