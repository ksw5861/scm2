package com.yedam.scm.order.service;

import java.util.List;

import com.yedam.scm.vo.ReturnVO;

public interface ReturnService {
    int insertReturn(ReturnVO returnVO);
    List<ReturnVO> getReturnList(String startDate, String endDate, String status);
    ReturnVO getReturnDetail(String returnId);
}