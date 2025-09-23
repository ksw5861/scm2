package com.yedam.scm.master.service;

import java.util.List;
import com.yedam.scm.vo.BomVO;

public interface BomService {

    // 기존에 인자 없는 getBomList()가 있으면 삭제하거나 이름 변경 가능
    List<BomVO> getBomList(BomVO bomVO);

    BomVO getBomDetail(String bomId);

    int insertBom(BomVO bom);

    int updateBom(BomVO bom);

    int deleteBom(String bomId);
}
