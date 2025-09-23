package com.yedam.scm.product.service;

import java.util.List;
import java.util.Map;
import com.yedam.scm.vo.ItemInboundVO;

public interface InboundService {
    List<ItemInboundVO> selectInboundLots(Map<String, Object> params);
    int insertInbound(ItemInboundVO vo);
    //int deleteInbound(String inboundId);
}
