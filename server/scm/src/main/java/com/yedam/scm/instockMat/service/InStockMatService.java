package com.yedam.scm.instockMat.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.yedam.scm.dto.AdjStockDTO;
import com.yedam.scm.dto.MatStockSearchDTO;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.dto.PurchaseListSearchDTO;
import com.yedam.scm.vo.InboundDetailVO;
import com.yedam.scm.vo.InboundLogVO;
import com.yedam.scm.vo.MatLotStockAdjVO;
import com.yedam.scm.vo.MatLotVO;
import com.yedam.scm.web.MatUnloadSearchDTO;

public interface InStockMatService {
    //하차대기(마스터)
    Map<String, Object> getVenShipList(PageDTO pageDTO, PurchaseListSearchDTO searchDTO);
    //하차대기(상세)
    List<InboundDetailVO> getVenShipDetailList(Long inboundId);
    //하차승인
    void callApproveUnload(Long inboundId, String unloadEmp);
    //하차반품
    void callUnloadReturn(Long inboundId, String unloadEmp, String rejMemo);
    //입고대기목록
    Map<String, Object> getApproveUnload(MatUnloadSearchDTO searchDTO, PageDTO pageDTO);
    //입고대기목록(상세)
    List<InboundDetailVO> getApproveUnloadDetailList(Long inboundId);
    //입고등록
    void callMatInboundStock(InboundDetailVO inStockInfo);
    //불량등록
    ResponseEntity<?> callRegMatDefect(InboundLogVO defectData, MultipartFile file);
    //재고목록
    Map<String, Object> getMatStockList(PageDTO pageDTO, MatStockSearchDTO searchDTO);
    //자재별LOT현황
    List<MatLotVO> getMatLotList(String matId);
    //재고조정등록 
    Long adjustMatStock(AdjStockDTO dto);
    //조정이력조회
    List<MatLotStockAdjVO> getAdjustHistoryByLotId(Long lotId);
}
