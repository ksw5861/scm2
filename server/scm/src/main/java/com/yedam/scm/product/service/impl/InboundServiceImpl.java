package com.yedam.scm.product.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Override
    public List<SalesOrderVO> getApprovalList(SalesOrderVO vo) {
        return inboundMapper.selectApprovalOrders(vo);
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

    /* ===================== 반품승인 ===================== */
    @Override
    public List<ReturnVO> getReturnList() {
        return inboundMapper.selectReturnList();
    }

    @Override
    public List<ReturnDetailVO> getReturnDetails(String returnId) {
        return inboundMapper.selectReturnDetails(returnId);
    }

    @Override
    @Transactional
    public int approveReturnDetails(List<String> ids) {
        if (ids == null || ids.isEmpty())
            return 0;
        return inboundMapper.approveReturnDetails(ids);
    }

    @Override
    @Transactional
    public int rejectReturnDetails(List<String> ids, String reason) {
        if (ids == null || ids.isEmpty())
            return 0;
        return inboundMapper.rejectReturnDetails(ids, reason);
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
    public Map<String, Object> getAccountLedger(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        List<SalesOrderVO> list = inboundMapper.selectAccountLedgerList(params);
        SalesOrderVO summary = inboundMapper.selectAccountLedgerSummary(params);
        result.put("items", list);
        result.put("summary", summary);
        return result;
    }

} // end
