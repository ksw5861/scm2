package com.yedam.scm.order.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.scm.master.mapper.VendorMapper;
import com.yedam.scm.order.mapper.OrderMapper;
import com.yedam.scm.order.service.OrderService;
import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.SalesOrderDetailVO;
import com.yedam.scm.vo.SalesOrderVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
   

    @Transactional
    @Override
    public boolean insertOrder(SalesOrderVO orderVO) {
        long totalPrice = 0L;
        if (orderVO.getDetails() != null) {
            for (SalesOrderDetailVO detail : orderVO.getDetails()) {
                totalPrice += (long) detail.getProdUnitPrice() * detail.getOrderQty();
            }
        }

        if (totalPrice <= 0) {
            orderVO.setFailReason("ZERO"); // ✅ 총액 0원
            return false;
        }

        orderVO.setTotalPrice(totalPrice);

        int creditLimit = orderMapper.getCreditLimit(orderVO.getVendorId());
        long remainCredit = creditLimit - totalPrice;
        orderVO.setRemainCredit(remainCredit < 0 ? creditLimit : remainCredit);

        if (remainCredit < 0) {
            orderVO.setFailReason("CREDIT"); // ✅ 여신 초과
            return false;
        }

        int masterResult = orderMapper.insertOrder(orderVO);
        if (masterResult <= 0) return false;

        for (SalesOrderDetailVO detail : orderVO.getDetails()) {
            detail.setOrderId(orderVO.getOrderId());
            if (orderMapper.insertOrderDetail(detail) <= 0) {
                return false;
            }
        }

        return true;
    }


    @Override
    public boolean insertOrderDetail(SalesOrderDetailVO detailVO) {
        return orderMapper.insertOrderDetail(detailVO) > 0;
    }

 


    // =============================================================
    // 조건별 주문 목록 조회
    // =============================================================
    @Override
    public List<SalesOrderVO> getOrderListForView(
        String vendorId,
        String startDate,
        String endDate,
        String prodName,
        String status,
        String orderId
    ) {
        return orderMapper.getOrderListForView(vendorId, startDate, endDate, prodName, status, orderId);
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
