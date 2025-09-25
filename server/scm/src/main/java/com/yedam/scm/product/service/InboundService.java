package com.yedam.scm.product.service;

import java.util.List;
import java.util.Map;

import com.yedam.scm.vo.ProductLotVO;

public interface InboundService {
    List<ProductLotVO> selectInboundLots(ProductLotVO vo);

    int updateInbound(ProductLotVO vo);

    int deleteInbound(String prdLot);

    boolean registerInbound(Map<String, Object> inbound);

}
