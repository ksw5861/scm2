package com.yedam.scm.product.service;

import java.util.List;
import java.util.Map;

import com.yedam.scm.dto.AccountLedgerSearchDTO;
import com.yedam.scm.dto.InboundListRes;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.dto.WarehouseListRes;
import com.yedam.scm.vo.ItemInboundVO;
import com.yedam.scm.vo.ReturnDetailVO;
import com.yedam.scm.vo.ReturnVO;
import com.yedam.scm.vo.SalesOrderDetailVO;
import com.yedam.scm.vo.SalesOrderVO;
// import com.yedam.scm.vo.ShipOrderVO;

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
    // List<SalesOrderVO> getApprovalList(SalesOrderVO vo);
    // List<SalesOrderVO> getApprovalList(AccountLedgerSearchDTO dto);
    // List<SalesOrderVO> getApprovalList(AccountLedgerSearchDTO dto, String
    // orderId);

    // 주문승인 목록 조회
    List<SalesOrderVO> getApprovalList(AccountLedgerSearchDTO dto, SalesOrderVO vo);

    List<SalesOrderDetailVO> getApprovalDetails(String orderId);

    int approveDetails(List<String> odetailIds);

    int rejectDetails(List<String> odetailIds);

    // 주문승인 프로시저 직접 호출 (반려 시 0원 처리용)
    void callProcApproveOrder(Map<String, Object> param);

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

    // 거래처원장 조회
    Map<String, Object> getAccountLedger(AccountLedgerSearchDTO condition);

    // 대시보드
    Map<String, Object> getDashboardSummary();

    List<SalesOrderVO> getDashboardList();

    // 주문승인 프로시저 실행 (상세 상태 변경 + 부모 합산/상태 반영)
    int approveOrderWithProc(List<String> odetailIds, String status);

    // 거래처원장 페이지 모달
    Map<String, Object> getVendorList(String keyword, int page, int size);

    /* ==============주문승인 페이지 모달 ============ */
    // ✅ 주문승인 - 판매처 모달
    Map<String, Object> getApprovalVendorModal(String condition, int page, int size);

    // ✅ 주문승인 - 주문번호 모달
    Map<String, Object> getApprovalOrderModal(String condition, int page, int size);



    /* ==============제품 반품 승인 페이지 모달 ============ */
     // 반품일자 검색 (기간)
    Map<String, Object> searchByDate(AccountLedgerSearchDTO dto, PageDTO paging);

    // 판매처명 검색
    Map<String, Object> searchByVendor(String condition, PageDTO paging);

    // 반품코드 검색
    Map<String, Object> searchByCode(String condition, PageDTO paging);





} //end
