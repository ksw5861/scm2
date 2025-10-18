package com.yedam.scm.master.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.yedam.scm.vo.WareHouseVO;

@Mapper
public interface WareHouseMapper1 {

    // 1. 창고 목록 조회
    List<WareHouseVO> getWareHouseList(
        @Param("whId") String whId,
        @Param("whName") String whName,
        @Param("status") String status
    );

    // 2. 창고 상세 조회
    WareHouseVO getWareHouseDetail(String whId);

    // 3. 창고 삭제
    int deleteWareHouse(String whId);

    // 4. 창고 등록
    int insertWareHouse(WareHouseVO wareHouseVO);

    // 5. 창고 수정
    int updateWareHouse(WareHouseVO wareHouseVO);

    // 자동완성
    List<String> autoCompleteWhName(String keyword);

}
