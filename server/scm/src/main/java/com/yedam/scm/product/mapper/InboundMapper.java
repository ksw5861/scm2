package com.yedam.scm.product.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.yedam.scm.vo.ProductLotVO;

@Mapper
public interface InboundMapper {
    // LOT 목록 조회
    List<ProductLotVO> selectInboundLots(ProductLotVO vo);

    // 입고 등록 (UPDATE)
    int updateInbound(ProductLotVO vo);

    // 입고 삭제
    int deleteInbound(String prdLot);

    int callInboundProcess(Map<String, Object> inbound);
}
