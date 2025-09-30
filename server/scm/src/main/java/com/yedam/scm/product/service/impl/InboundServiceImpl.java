package com.yedam.scm.product.service.impl;

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
import com.yedam.scm.vo.ReturnVO;
import com.yedam.scm.vo.SalesOrderDetailVO;
import com.yedam.scm.vo.SalesOrderVO;
import com.yedam.scm.vo.WareHouseVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InboundServiceImpl implements InboundService {

    private final InboundMapper inboundMapper;

    @Override
    public List<ItemInboundVO> selectInboundLots(ItemInboundVO vo) {
        return inboundMapper.selectInboundLots(vo);

    }

    @Override
    public boolean registerInbound(Map<String, Object> inbound) {

        // {
        // "prodNo": "PNO20250924-004",
        // "employeeId": "asd",
        // "totalQty": 10,
        // "whId": "asd"
        // }

        inboundMapper.callInsertInbound(inbound);

        // {
        // "prodNo": "PNO20250924-004",
        // "employeeId": "asd",
        // "totalQty": 10,
        // "whId": "asd"
        // "rowCount": 1
        // }

        return ((int) inbound.get("rowCount")) > 0; // 실행 성공하면 true 리턴
    }

    // 제품입고 페이지 삭제버튼
    @Override
    public int deleteInbound(String inboundId) {
        return inboundMapper.deleteInbound(inboundId);
    }

    // 모달 부분
    @Override
    public InboundListRes getInboundProductList(String condition, PageDTO paging) {
        // 목록 조회
        List<ProductVO> data = inboundMapper.selectInboundProductListByCondition(condition, paging);

        // 전체 건수 조회 후 PageDTO 업데이트
        long total = inboundMapper.selectInboundProductCountByCondition(condition);
        paging.updatePageInfo(total);

        // 응답 DTO 구성
        return new InboundListRes(data, paging);
        // 모달 부분//

    }

    // 제품입고창고 모달

    @Override
    public WarehouseListRes getWarehouseList(String condition, PageDTO paging) {
        // 목록
        List<WareHouseVO> data = inboundMapper.selectWarehouseListByCondition(condition, paging);
        // 총건수
        long total = inboundMapper.selectWarehouseCountByCondition(condition);
        // 페이징 채움
        paging.updatePageInfo(total);
        // 응답
        return new WarehouseListRes(data, paging);
    }

    // 주문승인페이지 목록불러오기

    @Override
    public List<SalesOrderVO> getApprovalList(SalesOrderVO vo) {
        return inboundMapper.selectApprovalOrders(vo);
    }




    // 주문승인 상세
    @Override
    public List<SalesOrderDetailVO> getApprovalDetails(String orderId) {
        return inboundMapper.selectApprovalDetails(orderId);
    }


    //주문승인 트리거
    @Override
    @Transactional
    public int approveDetails(List<String> odetailIds) {
        if (odetailIds == null || odetailIds.isEmpty())
            return 0;
        // 자식(DETAIL)만 업데이트 → 트리거가 부모(SALES_ORDER) 상태 동기화
        return inboundMapper.approveDetails(odetailIds);
    }


    //주문반려

    @Override
    @Transactional
    public int rejectDetails(List<String> odetailIds) {
        if (odetailIds == null || odetailIds.isEmpty()) return 0;
        return inboundMapper.rejectDetails(odetailIds);
    }



        //반품승인 페이지 목록불러오기
     @Override
    public List<ReturnVO> getReturnList() {
        return inboundMapper.selectReturnList();
    }


} // end
