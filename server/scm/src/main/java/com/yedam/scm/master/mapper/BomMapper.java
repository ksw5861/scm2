package com.yedam.scm.master.mapper;

import java.util.List;
import com.yedam.scm.vo.BomVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper

public interface BomMapper {

    // 특정 제품의 BOM 목록 조회
    List<BomVO> getBomByProdId(@Param("prodId") String prodId);

    // BOM 상세 조회 (bomId 기준)
    BomVO getBomDetail(@Param("bomId") String bomId);

    // BOM 등록
    int insertBom(BomVO bomVO);

    // BOM 수정
    int updateBom(BomVO bomVO);

    // BOM 삭제
    int deleteBom(@Param("bomId") String bomId);
}
