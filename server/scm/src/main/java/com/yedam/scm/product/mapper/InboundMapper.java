package com.yedam.scm.product.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import com.yedam.scm.vo.ItemInboundVO;

@Mapper
public interface InboundMapper {
    // LOT 목록 조회 (조건검색)
    List<ItemInboundVO> selectInboundLots(Map<String, Object> params);

    // 입고 등록
    int insertInbound(ItemInboundVO vo);

    // 입고 삭제
    int deleteInbound(String inboundId);
}
