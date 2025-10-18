package com.yedam.scm.master.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.yedam.scm.master.mapper.WareHouseMapper1;
import com.yedam.scm.master.service.WareHouseService1;
import com.yedam.scm.vo.WareHouseVO;

@Service
public class WareHouseServiceImpl1 implements WareHouseService1 {

    @Autowired
    WareHouseMapper1 mapper;

    @Override
    public List<WareHouseVO> getWareHouseList(String whId, String whName, String status) {
        return mapper.getWareHouseList(whId, whName, status);
    }

    @Override
    public WareHouseVO getWareHouseDetail(String whId) {
        return mapper.getWareHouseDetail(whId);
    }

    @Override
    public int deleteWareHouse(String whId) {
        return mapper.deleteWareHouse(whId);
    }

    @Override
    public int insertWareHouse(WareHouseVO wareHouseVO) {
        return mapper.insertWareHouse(wareHouseVO);
    }

    @Override
    public int updateWareHouse(WareHouseVO wareHouseVO) {
        return mapper.updateWareHouse(wareHouseVO);
    }
    @Override
    public List<String> autoCompleteWhName(String keyword) {
    return mapper.autoCompleteWhName(keyword);
}
}
