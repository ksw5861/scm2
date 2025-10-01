package com.yedam.scm.order.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.scm.order.mapper.ReturnMapper;
import com.yedam.scm.order.service.ReturnService;
import com.yedam.scm.vo.ReturnVO;
import com.yedam.scm.vo.ReturnDetailVO;

@Service
public class ReturnServiceImpl implements ReturnService {

    @Autowired
    private ReturnMapper returnMapper;

    // =============================================================
    // 반품 등록 (마스터 + 상세)
    // =============================================================
    @Override
    @Transactional
    public int insertReturn(ReturnVO returnVO) {
        int totalReturnPrice = 0;
        for (ReturnDetailVO detail : returnVO.getDetails()) {
            totalReturnPrice += detail.getProdUnitPrice() * detail.getReturnQty();
        }
        returnVO.setReturnPrice((long) totalReturnPrice);

        int masterResult = returnMapper.insertReturnOrder(returnVO);
        if (masterResult <= 0) {
            throw new RuntimeException("반품 마스터 등록 실패 - RETURN_ID: " + returnVO.getReturnId());
        }

        for (ReturnDetailVO detail : returnVO.getDetails()) {
            detail.setReturnId(returnVO.getReturnId());
            int detailResult = returnMapper.insertReturnDetail(detail);
            if (detailResult <= 0) {
                throw new RuntimeException("반품 상세 등록 실패 - PROD_ID: " + detail.getProdId());
            }
        }
        return masterResult;
    }

    // =============================================================
    // 반품 목록 조회
    // =============================================================
    @Override
    public List<ReturnVO> getReturnList(String startDate, String endDate,
                                        String returnStatus,  String returnId) {
        return returnMapper.getReturnList(startDate, endDate, returnStatus, returnId);
    }

    // =============================================================
    // 반품 단건 조회
    // =============================================================
    @Override
    public ReturnVO getReturnDetail(String returnId) {
        ReturnVO master = returnMapper.getReturnDetail(returnId);
        if (master == null) return null;

        List<ReturnDetailVO> details = returnMapper.getReturnDetailList(returnId);
        master.setDetails(details);
        return master;
    }

    // =============================================================
    // 반품 상세 목록 조회
    // =============================================================
    @Override
    public List<ReturnDetailVO> getReturnDetailList(String returnId) {
        return returnMapper.getReturnDetailList(returnId);
    }

    // =============================================================
    // 반품 가능 주문 조회
    // =============================================================
    @Override
    public List<ReturnVO> getReturnableOrders(String vendorId, String prodName) {
        return returnMapper.getReturnableOrders(vendorId, prodName);
    }

    // =============================================================
    // 반품 가능 주문 상세 조회
    // =============================================================
    @Override
    public List<ReturnDetailVO> getReturnableOrderDetails(String orderId) {
        return returnMapper.getReturnableOrderDetails(orderId);
    }

    // =============================================================
    // 반품 상태 변경
    // =============================================================
    @Transactional
    @Override
    public boolean updateReturnStatus(String returnId, String status) {
        int parent = returnMapper.updateReturnStatusParent(returnId, status);
        return parent > 0;
    }
}
