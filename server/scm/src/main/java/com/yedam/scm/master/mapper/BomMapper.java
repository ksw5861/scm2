package com.yedam.scm.master.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;

import com.yedam.scm.vo.BomVO;
import com.yedam.scm.vo.BomDetailVO;

@Mapper
public interface BomMapper {

    List<BomVO> getBomListByProdId(String prodId);

    BomVO getBomWithDetail(String bomId);

    int insertBom(BomVO bom);
    int insertBomDetail(BomDetailVO detail);
    int updateBom(BomVO bomVO);

    int updateBomDetail(BomDetailVO detail);

    int deleteBom(String bomId);
    int deleteBomDetailsByBomId(String bomId);

    List<String> autoCompletePrdName(String keyword);
}
