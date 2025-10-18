package com.yedam.scm.master.service;

import java.util.List;
import com.yedam.scm.vo.WareHouseVO;

public interface WareHouseService1 {
    List<WareHouseVO> getWareHouseList(String whId, String whName, String status);
    WareHouseVO getWareHouseDetail(String whId);
    int deleteWareHouse(String whId);
    int insertWareHouse(WareHouseVO wareHouseVO);
    int updateWareHouse(WareHouseVO wareHouseVO);
    List<String> autoCompleteWhName(String keyword);

}
