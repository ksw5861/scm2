package com.yedam.scm.order.service;


import org.springframework.stereotype.Service;


import com.yedam.scm.vo.SalesMarginVO;

import java.util.List;


public interface SalesMarginService {

    int save(SalesMarginVO vo);

    int saveAll(List<SalesMarginVO> list);


    List<SalesMarginVO> getList();

    SalesMarginVO getById(String saleProdId);

    

    int delete(String saleProdId);



}