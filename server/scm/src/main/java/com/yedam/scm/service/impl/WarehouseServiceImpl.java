package com.yedam.scm.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.scm.mapper.WarehouseMapper;
import com.yedam.scm.service.WarehouseService;
import com.yedam.scm.vo.WarehouseVO;

@Service
public class WarehouseServiceImpl implements WarehouseService {

  @Autowired
  WarehouseMapper mapper;

  @Override
  public List<WarehouseVO> getWarehouseList() {

    return mapper.selectWarehouseList();
  }


}
