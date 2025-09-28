package com.yedam.scm.product.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.vo.ItemInboundVO;
import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.SalesOrderVO;
import com.yedam.scm.vo.WareHouseVO;

@Mapper
public interface InboundMapper {
  // ✅ LOT 목록 조회 (조건검색) — 그대로 유지
  List<ItemInboundVO> selectInboundLots(ItemInboundVO vo);

  // ✅ 입고 등록: 프로시저 호출
  void callInsertInbound(Map<String, Object> vo);

  // ✅ 입고 삭제: item_inbound 기준 (컨트롤러가 inboundId 쓰고 있음)
  int deleteInbound(String inboundId);

  // 모달
  // 조건 + 페이징으로 제품 리스트 조회
  List<ProductVO> selectInboundProductListByCondition(
      @Param("condition") String condition,
      @Param("paging") PageDTO paging);

  // 조건에 맞는 총 데이터 건수 조회
  long selectInboundProductCountByCondition(
      @Param("condition") String condition);

  // 창고모달
  long selectWarehouseCountByCondition(@Param("condition") String condition);

  List<WareHouseVO> selectWarehouseListByCondition(
      @Param("condition") String condition,
      @Param("paging") PageDTO paging);

  // 주문승인 페이지 목록불러오기
  List<SalesOrderVO> selectApprovalOrders(SalesOrderVO vo);

}// end
