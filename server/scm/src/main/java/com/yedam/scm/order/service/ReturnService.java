package com.yedam.scm.order.service;

import java.util.List;

import com.yedam.scm.vo.ReturnVO;

public interface ReturnService {
    int insertReturn(ReturnVO returnVO);  // 기존 단건
    int insertReturnList(List<ReturnVO> returnList); // ✅ 다건용 추가

    List<ReturnVO> getReturnList(String startDate, String endDate, String returnStatus, String prodName, String returnNo);

    ReturnVO getReturnDetail(String returnId);
}
