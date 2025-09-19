package com.yedam.scm.master.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.scm.master.mapper.WareHouseMapper;
import com.yedam.scm.master.service.WareHouseService;
import com.yedam.scm.vo.WareHouseVO;

@Service
public class WareHouseServiceImpl implements WareHouseService {

  @Autowired
  WareHouseMapper mapper;

  @Override
  public List<WareHouseVO> getWareHouseList() {

    return mapper.selectWareHouseList();
  }

}
