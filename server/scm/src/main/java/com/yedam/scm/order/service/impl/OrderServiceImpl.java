package com.yedam.scm.order.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.scm.order.mapper.OrderMapper;
import com.yedam.scm.order.service.OrderService;
import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.SalesOrderDetailVO;
import com.yedam.scm.vo.SalesOrderVO;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    // =============================================================
    // 주문 등록 (마스터 + 상세)
    // =============================================================
    @Transactional
    @Override
    public boolean insertOrder(SalesOrderVO orderVO) {
        // 0원 체크 (상세 합계 계산)
        Long totalPrice = 0L;
        if (orderVO.getDetails() != null) {
            for (SalesOrderDetailVO detail : orderVO.getDetails()) {
                totalPrice += (long)detail.getProdUnitPrice() * detail.getOrderQty();
            }
        }

        if (totalPrice <= 0) {
            throw new RuntimeException("총액이 0원인 주문은 등록할 수 없습니다.");
        }

        // 마스터에 총액 세팅
        orderVO.setTotalPrice(totalPrice);

        int masterResult = orderMapper.insertOrder(orderVO);

        if (masterResult > 0 && orderVO.getDetails() != null && !orderVO.getDetails().isEmpty()) {
            for (SalesOrderDetailVO detail : orderVO.getDetails()) {
                detail.setOrderId(orderVO.getOrderId());
                int detailResult = orderMapper.insertOrderDetail(detail);
                if (detailResult <= 0) {
                    throw new RuntimeException("주문 상세 저장 실패");
                }
            }
            return true;
        }

        return masterResult > 0;
    }

    // =============================================================
    // 주문 상세 단건 등록
    // =============================================================
    @Override
    public boolean insertOrderDetail(SalesOrderDetailVO detailVO) {
        return orderMapper.insertOrderDetail(detailVO) > 0;
    }

    // =============================================================
    // 조건별 주문 목록 조회
    // =============================================================
    @Override
    public List<SalesOrderVO> getOrderListForView(String startDate,
                                                  String endDate,
                                                  String prodName,
                                                  String status,
                                                  String orderId) {
        return orderMapper.getOrderListForView(startDate, endDate, prodName, status, orderId);
    }

    // =============================================================
    // 특정 주문의 상세 내역 조회
    // =============================================================
    @Override
    public List<SalesOrderDetailVO> getOrderDetailList(String orderId) {
        return orderMapper.getOrderDetailList(orderId);
    }

    // =============================================================
    // 지점 대시보드 데이터 조회
    // =============================================================
    @Override
    public Map<String, Object> getBranchDashData() {
        return orderMapper.getBranchDashData();
    }

    // =============================================================
    // 제품 목록 조회 (모달용)
    // =============================================================
    @Override
    public List<ProductVO> getProductList(Map<String, Object> params) {
        return orderMapper.getProductList(params);
    }

    // =============================================================
    // 제품 전체 개수 조회
    // =============================================================
    @Override
    public int getProductCount() {
        return orderMapper.getProductCount();
    }

    // =============================================================
    // 주문 상태 변경
    // =============================================================
    @Override
    public boolean updateOrderStatus(String orderId, String status) {
        return orderMapper.updateStatus(orderId, status) > 0;
    }

    // =============================================================
    // 주문 취소
    // =============================================================
    @Transactional
    public int deleteOrderWithDetails(String orderId) {
        orderMapper.deleteOrderDetails(orderId);
        return orderMapper.deleteOrder(orderId);
    }   

    // =============================================================
    // 주문조회페이지 PDF
    // =============================================================
    public List<SalesOrderDetailVO> getOrderDetails(String orderId) {
        return orderMapper.findOrderDetails(orderId);
    }
}
