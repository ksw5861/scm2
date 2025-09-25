package com.yedam.scm.master.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.scm.master.mapper.UnitMapper;
import com.yedam.scm.master.service.UnitService;
import com.yedam.scm.vo.UnitVO;

@Service
public class UnitServiceImpl implements UnitService{
    @Autowired
    private UnitMapper unitMapper;
    
    public List<UnitVO> getUnitList() {
        return unitMapper.getUnitList();
    }
}