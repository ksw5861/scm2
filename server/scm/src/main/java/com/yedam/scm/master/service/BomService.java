package com.yedam.scm.master.service;

import java.util.List;
import com.yedam.scm.vo.BomVO;

public interface BomService {
    List<BomVO> getBomDetailByProdId(String prodId);
    BomVO getBomDetail(String bomId);
    int insertBom(BomVO bomVO);
    int updateBom(BomVO bomVO);
    int deleteBom(String bomId);
}
