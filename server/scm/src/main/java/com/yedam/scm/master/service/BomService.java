package com.yedam.scm.master.service;

import java.util.List;
import com.yedam.scm.vo.BomVO;
import com.yedam.scm.vo.BomDetailVO;

public interface BomService {

    // 조회
    List<BomVO> getBomDetailByProdId(String prodId);
    BomVO getBomDetail(String bomId);

    // 기본 CRUD
    int insertBom(BomVO bomVO);
    int updateBom(BomVO bomVO);
    int deleteBom(String bomId);

    // 부모-자식 트랜잭션용
    void insertBomWithDetails(BomVO bom, List<BomDetailVO> details);
    void updateBomWithDetails(BomVO bom, List<BomDetailVO> details);
    int deleteBomWithDetails(String bomId);

    // 자동 완성
    List<String> autoCompletePrdName(String keyword);

}
