package com.yedam.scm.product.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import com.yedam.scm.product.mapper.InboundMapper;
import com.yedam.scm.product.service.InboundService;
import com.yedam.scm.vo.ItemInboundVO;

@Service
@RequiredArgsConstructor
public class InboundServiceImpl implements InboundService {

    private final InboundMapper inboundMapper;

    @Override
    public List<ItemInboundVO> selectInboundLots(Map<String, Object> params) {
        return inboundMapper.selectInboundLots(params);
    }

    @Override
    public int insertInbound(ItemInboundVO vo) {
        return inboundMapper.insertInbound(vo);
    }

    // @Override
    // public int deleteInbound(String inboundId) {
    //     return inboundMapper.deleteInbound(inboundId);
    // }
}
