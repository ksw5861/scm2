package com.yedam.scm.order.service.impl;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.scm.order.mapper.OrderMapper;
import com.yedam.scm.order.service.OrderService;
import com.yedam.scm.vo.OrderVO;

@Service
public class OrderServiceImpl implements OrderService {

  @Autowired
  OrderMapper mapper;

  @Override
  public List<OrderVO> getOrderList() {

    return mapper.selectOrderList();
  }
  

}
