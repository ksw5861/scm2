package com.yedam.scm.web;

import org.springframework.web.bind.annotation.RestController;

import com.yedam.scm.service.WarehouseService;
import com.yedam.scm.vo.WarehouseVO;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class DhController {

  @Autowired
  WarehouseService svc;

  @GetMapping("/warehouse")
  public List<WarehouseVO> getMethodName() {
      return svc.getWarehouseList();
  }

}