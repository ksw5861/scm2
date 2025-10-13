package com.yedam.scm.instockMat.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yedam.scm.instockMat.mapper.InStockMatMapper;
import com.yedam.scm.instockMat.service.InStockMatService;
import com.yedam.scm.vo.InboundDetailVO;
import com.yedam.scm.vo.InboundVO;
import com.yedam.scm.vo.MatLotVO;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class InStockMatServiceImpl implements InStockMatService {
    
    final InStockMatMapper mapper;

    @Override
    public List<InboundVO> getVenShipList() {
        return mapper.getVenShipList();
    }

    @Override
    public List<InboundDetailVO> getVenShipDetailList(Long inboundId) {
       return mapper.getVenShipDetailList(inboundId);
    }

    @Override
    public void callApproveUnload(Long inboundId, String unloadEmp) {
        mapper.callApproveUnload(inboundId, unloadEmp);
    }

    @Override
    public void callUnloadReturn(Long inboundId, String unloadEmp, String rejMemo) {
        mapper.callUnloadReturn(inboundId, unloadEmp, rejMemo);
    }


    @Override
    public List<InboundVO> getApproveUnload() {
        return mapper.getApproveUnload();
    }

     @Override
    public List<InboundDetailVO> getApproveUnloadDetailList(Long inboundId) {
       return mapper.getApproveUnloadDetailList(inboundId);
    } 

    @Override
    public void callMatInboundStock(InboundDetailVO inStockInfo) {
        mapper.callMatInboundStock(inStockInfo);
    }

    @Override
    public List<MatLotVO> getMatStockList() {
        return mapper.getMatStockList();
    }

    @Override
    public List<MatLotVO> getMatLotList(String matId) {
       return mapper.getMatLotList(matId);
    }
}
