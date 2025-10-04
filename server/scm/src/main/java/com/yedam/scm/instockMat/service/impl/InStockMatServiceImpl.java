package com.yedam.scm.instockMat.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yedam.scm.instockMat.mapper.InStockMatMapper;
import com.yedam.scm.instockMat.service.InStockMatService;
import com.yedam.scm.vo.InboundVO;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class InStockMatServiceImpl implements InStockMatService {
    
    final InStockMatMapper mapper;

    @Override
    public List<InboundVO> getVenShipList() {
        return mapper.getVenShipList();
    } 

}
