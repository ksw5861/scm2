package com.yedam.scm.product.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.yedam.scm.vo.ItemInboundVO;

@Mapper
public interface InboundMapper {
      // ✅ LOT 목록 조회 (조건검색) — 그대로 유지
    List<ItemInboundVO> selectInboundLots(ItemInboundVO vo);

    // ✅ 입고 등록: 프로시저 호출
    void callInsertInbound(Map<String, Object> vo);

    // ✅ 입고 삭제: item_inbound 기준 (컨트롤러가 inboundId 쓰고 있음)
    int deleteInbound(String inboundId);
}
