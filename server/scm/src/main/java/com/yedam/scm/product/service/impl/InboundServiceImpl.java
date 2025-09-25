package com.yedam.scm.product.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yedam.scm.product.mapper.InboundMapper;
import com.yedam.scm.product.service.InboundService;
import com.yedam.scm.vo.ProductLotVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InboundServiceImpl implements InboundService {

    private final InboundMapper inboundMapper;

    @Override
    public List<ProductLotVO> selectInboundLots(ProductLotVO vo) {
        return inboundMapper.selectInboundLots(vo);
    }

    @Override
    public int updateInbound(ProductLotVO vo) {
        return inboundMapper.updateInbound(vo);

    }

    @Override
    public int deleteInbound(String inboundId) {
        return inboundMapper.deleteInbound(inboundId);
    }

    @Override
    public boolean registerInbound(Map<String, Object> inbound) {
        return inboundMapper.callInboundProcess(inbound) > 0;

    }
}