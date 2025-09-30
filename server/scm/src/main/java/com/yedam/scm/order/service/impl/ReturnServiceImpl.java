package com.yedam.scm.order.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.scm.order.mapper.ReturnMapper;
import com.yedam.scm.order.service.ReturnService;
import com.yedam.scm.vo.*;

@Service
public class ReturnServiceImpl implements ReturnService {

    @Autowired
    private ReturnMapper returnMapper;

    /**
     * =============================================================
     * 1. 반품 등록 (마스터 + 상세)
     * =============================================================
     */
    @Override
    @Transactional
    public int insertreturn(ReturnVO returnVO) {

        // 1. 반품 합계 금액 계산
        int totalReturnPrice = 0;
        for (ReturnDetailVO detail : returnVO.getDetails()) {
            totalReturnPrice += detail.getProdUnitPrice() * detail.getReturnQty();
        }
        returnVO.setReturnPrice((long) totalReturnPrice);

        // 2. RETURN 마스터 등록
        int masterResult = returnMapper.insertReturnOrder(returnVO);
        if (masterResult <= 0) {
            throw new RuntimeException("반품 마스터 등록 실패 - RETURN_ID: " + returnVO.getReturnId());
        }

        // 3. RETURN_DETAIL 반복 등록
        for (ReturnDetailVO detail : returnVO.getDetails()) {
            detail.setReturnId(returnVO.getReturnId()); // FK 설정

            int detailResult = returnMapper.insertReturnDetail(detail);
            if (detailResult <= 0) {
                throw new RuntimeException("반품 상세 등록 실패 - PROD_ID: " + detail.getProdId());
            }
        }

        return masterResult;
    }

    /**
     * =============================================================
     * 2. 반품 목록 조회 (마스터 기준)
     * =============================================================
     */
    @Override
    public List<ReturnVO> getReturnList(String startDate, String endDate,
                                        String returnStatus, String prodName, String returnId) {
        return returnMapper.getReturnList(startDate, endDate, returnStatus, prodName, returnId);
    }

    /**
     * =============================================================
     * 3. 반품 단건 조회 (마스터 + 상세)
     * =============================================================
     */
    @Override
    public ReturnVO getReturnDetail(String returnId) {
        // 1. 마스터 조회
        ReturnVO master = returnMapper.getReturnDetail(returnId);
        if (master == null) {
            return null;
        }

        // 2. 상세 조회
        List<ReturnDetailVO> details = returnMapper.getReturnDetailList(returnId);

        // 3. 마스터 객체에 상세 리스트 추가
        master.setDetails(details);
        return master;
    }

    /**
     * =============================================================
     * 4. 반품 상세 목록 단독 조회
     * =============================================================
     */
    @Override
    public List<ReturnDetailVO> getReturnDetailList(String returnId) {
        return returnMapper.getReturnDetailList(returnId);
    }

    /**
     * =============================================================
     * 5. 반품 가능 주문 + 상세 조회
     * - 두 쿼리 방식
     * =============================================================
     */
    @Override
    public List<ReturnVO> getReturnableOrders(String vendorId, String prodName) {

        // 1) 반품 가능 주문 마스터 조회
        List<ReturnVO> orders = returnMapper.getReturnableOrders(vendorId, prodName);

        if (orders.isEmpty()) {
            return orders;
        }

        return orders;
    }

    /**
     * =============================================================
     * 6. 반품 가능 주문 상세 조회
     * =============================================================
     */
    @Override
    public List<ReturnDetailVO> getReturnableOrderDetails(String orderIds) {
        return returnMapper.getReturnableOrderDetails(orderIds);
    }

    /**
     * =============================================================
     * 7. 반품 상태 변경
     * =============================================================
     */ 
    @Override
    public boolean updateReturnStatus(String returnId, String status) {
        return returnMapper.updateStatus(returnId, status) > 0;
    }

}
