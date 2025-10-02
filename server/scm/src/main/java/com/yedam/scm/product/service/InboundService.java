package com.yedam.scm.product.service;

import java.util.List;
import java.util.Map;

import com.yedam.scm.dto.InboundListRes;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.dto.WarehouseListRes;
import com.yedam.scm.vo.ItemInboundVO;
import com.yedam.scm.vo.ReturnDetailVO;
import com.yedam.scm.vo.ReturnVO;
import com.yedam.scm.vo.SalesOrderDetailVO;
import com.yedam.scm.vo.SalesOrderVO;
import com.yedam.scm.vo.ShipOrderVO;

public interface InboundService {

    /* ===================== 제품입고 ===================== */
    List<ItemInboundVO> selectInboundLots(ItemInboundVO vo);

    int deleteInbound(String inboundId);

    boolean registerInbound(Map<String, Object> inbound);

    // 모달에서 사용할 제품 목록 조회
    InboundListRes getInboundProductList(String condition, PageDTO paging);

    // 제품입고 창고모달
    WarehouseListRes getWarehouseList(String condition, PageDTO paging);


    /* ===================== 주문승인 ===================== */
    List<SalesOrderVO> getApprovalList(SalesOrderVO vo);

    List<SalesOrderDetailVO> getApprovalDetails(String orderId);

    int approveDetails(List<String> odetailIds);

    int rejectDetails(List<String> odetailIds);


    /* ===================== 반품승인 ===================== */
    List<ReturnVO> getReturnList();

    List<ReturnDetailVO> getReturnDetails(String returnId);

    int approveReturnDetails(List<String> ids);

    int rejectReturnDetails(List<String> ids, String reason);


/* ===================== 출하지시 ===================== */

// 출하지시 대상 목록 (주문건 기준)
List<SalesOrderVO> getShipOrders(SalesOrderVO vo);

// 여러 주문건 출하지시 등록 (헤더 단위)
int createShipOrders(List<SalesOrderVO> orders);

// 단일 주문 부분출고 등록 (상세 단위)
int createShipOrderDetails(List<SalesOrderDetailVO> details);

}
