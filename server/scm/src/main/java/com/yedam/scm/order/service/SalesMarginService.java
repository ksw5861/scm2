package com.yedam.scm.order.service;


import org.springframework.stereotype.Service;


import com.yedam.scm.vo.SalesMarginVO;

import java.util.List;

@Service
public interface SalesMarginService {

    

    List<SalesMarginVO> getList();
    void save(SalesMarginVO vo);           // 단건 저장(업서트)
    void saveAll(List<SalesMarginVO> list); // 일괄 저장(업서트)
    void delete(String saleProdId);

}