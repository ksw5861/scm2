package com.yedam.scm.web;

import org.springframework.web.bind.annotation.RestController;

import com.yedam.scm.master.service.WareHouseService;
import com.yedam.scm.vo.WareHouseVO;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class DhController {

  @Autowired
  WareHouseService svc;

  @GetMapping("/warehouse")
  public List<WareHouseVO> getWareHouseList() {
    return svc.getWareHouseList();
  }

}