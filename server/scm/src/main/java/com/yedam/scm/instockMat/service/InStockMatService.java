package com.yedam.scm.instockMat.service;

import java.util.List;

import com.yedam.scm.vo.InboundDetailVO;
import com.yedam.scm.vo.InboundVO;

public interface InStockMatService {
    //하차대기(마스터)
    List<InboundVO> getVenShipList();
    //하차대기(상세)
    List<InboundDetailVO> getVenShipDetailList(Long inboundId);
    //하차승인
    void callApproveUnload(Long inboundId, String unloadEmp);
    //입고대기목록
    List<InboundVO> getApproveUnload();
    //입고대기목록(상세)
    List<InboundDetailVO> getApproveUnloadDetailList(Long inboundId);
    //입고등록
    void callMatInboundStock(InboundDetailVO inStockInfo);
}
