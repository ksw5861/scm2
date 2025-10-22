package com.yedam.scm.product.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.yedam.scm.dto.AccountLedgerSearchDTO;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.vo.ItemInboundVO;
import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.ReturnDetailVO;
import com.yedam.scm.vo.ReturnVO;
import com.yedam.scm.vo.SalesOrderDetailVO;
import com.yedam.scm.vo.SalesOrderVO;
import com.yedam.scm.vo.VendorVO;
// import com.yedam.scm.vo.ShipOrderVO;
import com.yedam.scm.vo.WareHouseVO;

@Mapper
public interface InboundMapper {

        /* ===================== 제품입고 ===================== */

        // LOT 목록 조회
        List<ItemInboundVO> selectInboundLots(ItemInboundVO vo);

        // 입고 등록 (프로시저)
        void callInsertInbound(Map<String, Object> vo);

        // 입고 삭제
        int deleteInbound(String inboundId);

        // 제품 모달 조회
        List<ProductVO> selectInboundProductListByCondition(
                        @Param("condition") String condition,
                        @Param("paging") PageDTO paging);

        long selectInboundProductCountByCondition(@Param("condition") String condition);

        // 창고 모달
        long selectWarehouseCountByCondition(@Param("condition") String condition);

        List<WareHouseVO> selectWarehouseListByCondition(
                        @Param("condition") String condition,
                        @Param("paging") PageDTO paging);

        /* ===================== 주문승인 ===================== */

        // 주문승인 목록
        // List<SalesOrderVO> selectApprovalOrders(SalesOrderVO vo);
        // List<SalesOrderVO> selectApprovalOrders(AccountLedgerSearchDTO dto);
        // List<SalesOrderVO> selectApprovalOrders(
        // @Param("dto") AccountLedgerSearchDTO dto,
        // @Param("orderId") String orderId);
        List<SalesOrderVO> selectApprovalOrders(
                        @Param("dto") AccountLedgerSearchDTO dto,
                        @Param("vo") SalesOrderVO vo);

        // 주문승인 상세
        List<SalesOrderDetailVO> selectApprovalDetails(@Param("orderId") String orderId);

        // 승인/반려 처리
        int approveDetails(@Param("odetailIds") List<String> odetailIds);

        int rejectDetails(@Param("odetailIds") List<String> odetailIds);

        // 주문승인 프로시저 실행 (상세 상태 변경 + 부모 합산/상태 반영)
        void callProcApproveOrder(Map<String, Object> param);

        /* ===================== 반품승인 ===================== */

        // 반품 목록
        List<ReturnVO> selectReturnList();

        // 반품 상세
        List<ReturnDetailVO> selectReturnDetails(@Param("returnId") String returnId);

        // 승인/반려 처리
        int approveReturnDetails(@Param("ids") List<String> ids);

        int rejectReturnDetails(@Param("ids") List<String> ids, @Param("reason") String reason);

        int updateReturnStatus(@Param("returnId") String returnId);

        /* ===================== 출하지시 ===================== */

        // 출하지시 대상 주문 목록
        List<SalesOrderVO> selectShippingOrders(SalesOrderVO vo);

        // 여러 주문건 출하지시 등록 (헤더 단위)
        int insertShipOrders(@Param("orders") List<SalesOrderVO> orders);

        // 단일 주문 부분출고 등록 (상세 단위)
        int insertShipOrderDetails(@Param("details") List<SalesOrderDetailVO> details);

        // 거래처원장 목록 조회
        List<SalesOrderVO> selectAccountLedgerList(AccountLedgerSearchDTO condition);

        // 요약 합계(총매출/총반품/총입금/총미수금/총미지급)
        SalesOrderVO selectAccountLedgerSummary(Map<String, Object> params);

        // 대시보드
        // Map<String, Object> selectDashboardSummary();
        SalesOrderVO selectDashboardSummary();

        List<SalesOrderVO> selectDashboardList();

        // 거래처원장 페이지 모달
        List<VendorVO> selectVendorListByCondition(@Param("condition") String condition,
                        @Param("paging") PageDTO paging);

        long selectVendorCountByCondition(@Param("condition") String condition);

        /* ============주문승인 페이지 모달============ */
        List<VendorVO> selectApprovalVendorModal(
                        @Param("condition") String condition,
                        @Param("paging") PageDTO paging);

        long countApprovalVendorModal(@Param("condition") String condition);

        List<SalesOrderVO> selectApprovalOrderModal(
                        @Param("condition") String condition,
                        @Param("paging") PageDTO paging);

        long countApprovalOrderModal(@Param("condition") String condition);
}
