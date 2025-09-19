package com.yedam.scm.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.scm.service.OrderService;
import com.yedam.scm.vo.OrderVO;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class EgController {
  
  @Autowired
  OrderService svc;

  @GetMapping("/insertorder")
  public List<OrderVO> getMethodName() {
      return svc.getOrderList();
  }
  
}

