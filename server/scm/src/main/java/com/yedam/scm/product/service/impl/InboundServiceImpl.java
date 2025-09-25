package com.yedam.scm.product.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yedam.scm.product.mapper.InboundMapper;
import com.yedam.scm.product.service.InboundService;
import com.yedam.scm.vo.ItemInboundVO;
import com.yedam.scm.vo.ProductLotVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InboundServiceImpl implements InboundService {

    private final InboundMapper inboundMapper;

 
    @Override
    public List<ItemInboundVO> selectInboundLots(ItemInboundVO vo){
        return inboundMapper.selectInboundLots(vo);
        
    }


      @Override
    public boolean registerInbound(Map<String,Object> inbound) {
        inboundMapper.callInsertInbound(inbound);
        
        return ((int) inbound.get("rowCount")) > 0; // 실행 성공하면 true 리턴
    }
    


    @Override
    public int deleteInbound(String inboundId) {
        return inboundMapper.deleteInbound(inboundId);
    }


}
