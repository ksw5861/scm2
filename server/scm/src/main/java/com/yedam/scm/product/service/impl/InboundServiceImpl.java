package com.yedam.scm.product.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.scm.dto.AccountLedgerSearchDTO;
import com.yedam.scm.dto.InboundListRes;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.dto.WarehouseListRes;
import com.yedam.scm.product.mapper.InboundMapper;
import com.yedam.scm.product.service.InboundService;
import com.yedam.scm.vo.ItemInboundVO;
import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.ReturnDetailVO;
import com.yedam.scm.vo.ReturnVO;
import com.yedam.scm.vo.SalesOrderDetailVO;
import com.yedam.scm.vo.SalesOrderVO;
import com.yedam.scm.vo.VendorVO;
// import com.yedam.scm.vo.ShipOrderVO;
import com.yedam.scm.vo.WareHouseVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InboundServiceImpl implements InboundService {

    private final InboundMapper inboundMapper;

    /* ===================== 제품입고 ===================== */
    @Override
    public List<ItemInboundVO> selectInboundLots(ItemInboundVO vo) {
        return inboundMapper.selectInboundLots(vo);
    }

    @Override
    public boolean registerInbound(Map<String, Object> inbound) {
        inboundMapper.callInsertInbound(inbound);
        return ((int) inbound.get("rowCount")) > 0;
    }

    @Override
    public int deleteInbound(String inboundId) {
        return inboundMapper.deleteInbound(inboundId);
    }

    @Override
    public InboundListRes getInboundProductList(String condition, PageDTO paging) {
        List<ProductVO> data = inboundMapper.selectInboundProductListByCondition(condition, paging);
        long total = inboundMapper.selectInboundProductCountByCondition(condition);
        paging.updatePageInfo(total);
        return new InboundListRes(data, paging);
    }

    @Override
    public WarehouseListRes getWarehouseList(String condition, PageDTO paging) {
        List<WareHouseVO> data = inboundMapper.selectWarehouseListByCondition(condition, paging);
        long total = inboundMapper.selectWarehouseCountByCondition(condition);
        paging.updatePageInfo(total);
        return new WarehouseListRes(data, paging);
    }

    /* ===================== 주문승인 ===================== */
    // @Override
    // public List<SalesOrderVO> getApprovalList(SalesOrderVO vo) {
    // return inboundMapper.selectApprovalOrders(vo);
    // }
    // @Override
    // public List<SalesOrderVO> getApprovalList(AccountLedgerSearchDTO dto) {
    // return inboundMapper.selectApprovalOrders(dto);
    // }
    // @Override
    // public List<SalesOrderVO> getApprovalList(AccountLedgerSearchDTO dto, String
    // orderId) {
    // return inboundMapper.selectApprovalOrders(dto, orderId);
    // }
    @Override
    public List<SalesOrderVO> getApprovalList(AccountLedgerSearchDTO dto, SalesOrderVO vo) {
        return inboundMapper.selectApprovalOrders(dto, vo);
    }

    @Override
    public List<SalesOrderDetailVO> getApprovalDetails(String orderId) {
        return inboundMapper.selectApprovalDetails(orderId);
    }

    @Override
    @Transactional
    public int approveDetails(List<String> odetailIds) {
        if (odetailIds == null || odetailIds.isEmpty())
            return 0;
        return inboundMapper.approveDetails(odetailIds);
    }

    @Override
    @Transactional
    public int rejectDetails(List<String> odetailIds) {
        if (odetailIds == null || odetailIds.isEmpty())
            return 0;
        return inboundMapper.rejectDetails(odetailIds);
    }

    // 주문승인 프로시저 다시
    // @Override
    // @Transactional
    // public int approveOrderWithProc(List<String> odetailIds, String status) {
    // if (odetailIds == null || odetailIds.isEmpty()) return 0;

    // // 콤마로 이어붙인 문자열로 변환
    // String joinedIds = String.join(",", odetailIds);

    // Map<String, Object> param = new HashMap<>();
    // param.put("orderDetailIds", joinedIds); // 문자열로 전달
    // param.put("status", status);
    // param.put("rowCount", 0);

    // inboundMapper.callProcApproveOrder(param);

    // return (int) param.get("rowCount");
    // }

    @Override
    @Transactional
    public void callProcApproveOrder(Map<String, Object> param) {
        inboundMapper.callProcApproveOrder(param);
    }

    /* ===================== 반품승인 ===================== */
    @Override
    public List<ReturnVO> getReturnList() {
        return inboundMapper.selectReturnList();
    }

    @Override
    public List<ReturnDetailVO> getReturnDetails(String returnId) {
        return inboundMapper.selectReturnDetails(returnId);
    }

    // 반품승인
    @Override
    @Transactional
    public int approveReturnDetails(List<String> ids) {
        if (ids == null || ids.isEmpty())
            return 0;
        int result = inboundMapper.approveReturnDetails(ids);
        inboundMapper.updateReturnStatus(ids.get(0));
        return result;
    }

    // 반품반려
    @Override
    @Transactional
    public int rejectReturnDetails(List<String> ids, String reason) {
        if (ids == null || ids.isEmpty())
            return 0;

        int result = inboundMapper.rejectReturnDetails(ids, reason);
        inboundMapper.updateReturnStatus(ids.get(0));
        return result;
    }

    /* ===================== 출하지시 ===================== */
    // 출하지시 대상 조회
    @Override
    public List<SalesOrderVO> getShipOrders(SalesOrderVO vo) {
        return inboundMapper.selectShippingOrders(vo);
    }

    // 여러 주문건 출하지시 등록 (헤더 단위)
    @Override
    @Transactional
    public int createShipOrders(List<SalesOrderVO> orders) {
        if (orders == null || orders.isEmpty())
            return 0;
        return inboundMapper.insertShipOrders(orders);
    }

    // 단일 주문 부분출고 등록 (상세 단위)
    @Override
    @Transactional
    public int createShipOrderDetails(List<SalesOrderDetailVO> details) {
        if (details == null || details.isEmpty())
            return 0;
        return inboundMapper.insertShipOrderDetails(details);
    }

    // 거래처원장
    @Override
    public Map<String, Object> getAccountLedger(AccountLedgerSearchDTO condition) {
        Map<String, Object> result = new HashMap<>();
        List<SalesOrderVO> list = inboundMapper.selectAccountLedgerList(condition);
        result.put("items", list);
        return result;
    }

    // @Override
    // public Map<String, Object> getDashboardSummary() {

    // // return inboundMapper.selectDashboardSummary();
    // }

    @Override
    public List<SalesOrderVO> getDashboardList() {

        return inboundMapper.selectDashboardList();
    }

    @Override
    @Transactional
    public int approveOrderWithProc(List<String> odetailIds, String status) {
        if (odetailIds == null || odetailIds.isEmpty())
            return 0;

        // 콤마로 이어붙인 문자열로 변환
        String joinedIds = String.join(",", odetailIds);

        Map<String, Object> param = new HashMap<>();
        param.put("orderDetailIds", joinedIds); // 문자열로 전달
        param.put("status", status);
        param.put("rowCount", 0);

        inboundMapper.callProcApproveOrder(param);

        return (int) param.get("rowCount");
    }

    @Override
    public Map<String, Object> getDashboardSummary() {
        SalesOrderVO vo = inboundMapper.selectDashboardSummary();

        Map<String, Object> map = new HashMap<>();
        map.put("totalPrice", vo.getTotalPrice());
        map.put("returnPrice", vo.getReturnPrice());
        map.put("totalPayment", vo.getTotalPayment());
        map.put("totalAr", vo.getTotalAr());
        return map;
    }

    // 거래처원장 페이지 모달
    @Override
    public Map<String, Object> getVendorList(String keyword, int page, int size) {
        PageDTO paging = new PageDTO(page, size);

        long total = inboundMapper.selectVendorCountByCondition(keyword);
        paging.updatePageInfo(total);

        List<VendorVO> list = inboundMapper.selectVendorListByCondition(keyword, paging);

        Map<String, Object> result = new HashMap<>();
        result.put("data", list);
        result.put("page", paging);
        return result;
    }

    // 주문승인 페이지 모달
    @Override
    public Map<String, Object> getApprovalVendorModal(String condition, int page, int size) {
        PageDTO paging = new PageDTO(page, size);
        long total = inboundMapper.countApprovalVendorModal(condition);
        paging.updatePageInfo(total);
        List<VendorVO> list = inboundMapper.selectApprovalVendorModal(condition, paging);

        Map<String, Object> result = new HashMap<>();
        result.put("data", list);
        result.put("page", paging);
        return result;
    }

    @Override
    public Map<String, Object> getApprovalOrderModal(String condition, int page, int size) {
        PageDTO paging = new PageDTO(page, size);
        long total = inboundMapper.countApprovalOrderModal(condition);
        paging.updatePageInfo(total);
        List<SalesOrderVO> list = inboundMapper.selectApprovalOrderModal(condition, paging);

        Map<String, Object> result = new HashMap<>();
        result.put("data", list);
        result.put("page", paging);
        return result;
    }






        // ===================== 1️⃣ 반품일자 모달 =====================
    @Override
    public Map<String, Object> searchByDate(AccountLedgerSearchDTO dto, PageDTO paging) {
        int total = inboundMapper.countByDate(dto);
        paging.updatePageInfo(total);

        Map<String, Object> result = new HashMap<>();
        result.put("items", inboundMapper.searchByDate(dto, paging));
        result.put("totalCount", total);
        return result;
    }

   // ===================== 2️⃣ 판매처명 모달 =====================
@Override
public Map<String, Object> searchByVendor(String condition, PageDTO paging) {
    // 1) 총 개수
    int total = inboundMapper.countByVendor(condition);

    // 2) 페이지/사이즈 안전 계산 (page, size가 null/0이어도 방어)
    final int page = (paging != null && paging.getPage() > 0) ? paging.getPage() : 1;
    final int size = (paging != null && paging.getSize() > 0) ? paging.getSize() : 10;
    final int offset = (page - 1) * size;

    // 3) 데이터 조회 (offset/size 직접 전달)
    Map<String, Object> result = new HashMap<>();
    result.put("items", inboundMapper.searchByVendor(condition, offset, size));
    result.put("totalCount", total);
    return result;
}

// ===================== 3️⃣ 반품코드 모달 =====================
@Override
public Map<String, Object> searchByCode(String condition, PageDTO paging) {
    int total = inboundMapper.countByCode(condition);

    final int page = (paging != null && paging.getPage() > 0) ? paging.getPage() : 1;
    final int size = (paging != null && paging.getSize() > 0) ? paging.getSize() : 10;
    final int offset = (page - 1) * size;

    Map<String, Object> result = new HashMap<>();
    result.put("items", inboundMapper.searchByCode(condition, offset, size));
    result.put("totalCount", total);
    return result;
}









}// end
