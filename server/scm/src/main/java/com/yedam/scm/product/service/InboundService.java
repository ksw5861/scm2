package com.yedam.scm.product.service;

import java.util.List;
import java.util.Map;

import com.yedam.scm.dto.InboundListRes;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.dto.WarehouseListRes;
import com.yedam.scm.vo.ItemInboundVO;
import com.yedam.scm.vo.SalesOrderDetailVO;
import com.yedam.scm.vo.SalesOrderVO;

public interface InboundService {
  List<ItemInboundVO> selectInboundLots(ItemInboundVO vo);

  int deleteInbound(String inboundId);

  boolean registerInbound(Map<String, Object> inbound);

  // 인터페이스 작성 후 클래스 작성시 유의사항
  // 1. return data type
  // 2. args data 갯수
  // 3. args data type

  // => 위 내용에 따라서 메소드 이름이 같더라도
  // 다 다른 메소드

  // 모달에서 사용할 제품 목록 조회
  InboundListRes getInboundProductList(String condition, PageDTO paging);

  // 제품입고 창고모달
  WarehouseListRes getWarehouseList(String condition, PageDTO paging);

  List<SalesOrderVO> getApprovalList(SalesOrderVO vo);

  // 주문승인 상세
  List<SalesOrderDetailVO> getApprovalDetails(String orderId);

  // 주문승인 트리거
  int approveDetails(List<String> odetailIds);


  // 주문승인 반려 처리
int rejectDetails(List<String> odetailIds);

}
