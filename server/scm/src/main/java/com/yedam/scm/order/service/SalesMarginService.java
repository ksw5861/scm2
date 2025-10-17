package com.yedam.scm.order.service;


import org.springframework.stereotype.Service;


import com.yedam.scm.vo.SalesMarginVO;
import com.yedam.scm.vo.SalesMasterVO;

import java.util.List;
import java.util.Map;


public interface SalesMarginService {

    // int save(SalesMarginVO vo);

    // 포스설정에서 기존데이터삭제후 모두 새로저장
    int saveAll(List<SalesMarginVO> list);

    // 전체 목록 조회
    List<SalesMarginVO> getList();

    SalesMarginVO getById(String saleProdId);

    

    int delete(String saleProdId);

    //포스기 화면에서 결제등록
    void registerSale(SalesMasterVO vo);

    // 매출이력조회
    List<SalesMasterVO> getSalesHistory(String vendorId);

}