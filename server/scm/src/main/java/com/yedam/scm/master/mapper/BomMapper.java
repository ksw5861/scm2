package com.yedam.scm.master.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.yedam.scm.vo.BomVO;
import com.yedam.scm.vo.BomDetailVO;

@Mapper
public interface BomMapper {
    List<BomVO> getBomByProdId(@Param("prodId") String prodId);
    BomVO getBomDetail(@Param("bomId") String bomId);

    int insertBom(BomVO bomVO);
    int insertBomDetail(BomDetailVO detail);
    int updateBom(BomVO bomVO);
    int updateBomDetail(BomDetailVO detail);
    int deleteBom(@Param("bomId") String bomId);
    int deleteBomDetailsByBomId(@Param("bomId") String bomId);
    List<String> autoCompletePrdName(String keyword);

}

