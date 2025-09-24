package com.yedam.scm.master.mapper;

import java.util.List;
import com.yedam.scm.vo.BomVO;


public interface BomMapper {
    int insertBom(BomVO bom);                // BOM 등록
    int updateBom(BomVO bom);                // BOM 수정
    int deleteBom(String bomId);             // BOM 삭제
    List<BomVO> getBomByProdId(String prodId); // 제품별 BOM 조회
    List<BomVO> getBomList(BomVO bomVO);
    BomVO getBomDetail(String bomId);
}
