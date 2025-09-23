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

/**
 * OrderServiceImpl
 * ===========================
 * 주문 마스터 및 상세 데이터의 비즈니스 로직 구현
 * - 주문 등록(마스터 + 상세)
 * - 주문 목록 조회
 * - 주문 상세 조회
 * - 제품 목록 조회 (모달용)
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    // ===============================================================
    // 1. 주문 등록 (마스터 + 상세) - 트랜잭션 적용
    // ===============================================================
    @Transactional // 하나라도 실패 시 전체 롤백
    @Override
    public int insertOrder(SalesOrderVO orderVO) {

        // 1) 주문 마스터 저장
        int result = orderMapper.insertOrder(orderVO);

        // 2) 주문 상세 저장 (마스터 저장 성공 시)
        if (result > 0 && orderVO.getDetails() != null) {

            for (SalesOrderDetailVO detail : orderVO.getDetails()) {

                // 주문번호 주입
                detail.setOrderId(orderVO.getOrderId());

                // 상세 합계 계산 (수량 * 단가)
                detail.setTotalPrice(detail.getProdPrice() * detail.getOrderQty());

                // 상세 저장
                orderMapper.insertOrderDetail(detail);
            }
        }

        return result;
    }

    // ===============================================================
    // 2. 주문 상세 단건 등록
    // ===============================================================
    @Override
    public int insertOrderDetail(SalesOrderDetailVO detailVO) {
        return orderMapper.insertOrderDetail(detailVO);
    }

    // ===============================================================
    // 3. 주문 상세 조회 (odetail_id 기준 단건 or 리스트)
    // ===============================================================
    // @Override
    // public List<SalesOrderDetailVO> getOrderDetails(String orderId) {
    //     return orderMapper.getOrderDetails(orderId);
    // }

    // ===============================================================
    // 4. 주문 목록 조회 (검색 조건 포함)
    // ===============================================================
    @Override
    public List<SalesOrderVO> getOrderListForView(
            String startDate,
            String endDate,
            String prodName,
            String status,
            String orderId) {

        return orderMapper.getOrderListForView(startDate, endDate, prodName, status, orderId);
    }

    // ===============================================================
    // 5. 지점 대시보드 데이터 조회
    // ===============================================================
    @Override
    public Object getBranchDashData() {
        return orderMapper.getBranchDashData();
    }

    // ===============================================================
    // 6. 주문 상세 조회 (order_id 기준 전체 상세)
    // ===============================================================
    @Override
    public List<SalesOrderDetailVO> getOrderDetailList(String orderId) {
        return orderMapper.getOrderDetailList(orderId);
    }

    // ===============================================================
    // 7. 제품 목록 조회 (모달용)
    // ===============================================================
    @Override
    public List<ProductVO> getProductList(String prodName, int offset, int pageSize) {
    Map<String, Object> params = new HashMap<>();
    params.put("prodName", prodName);   // ✅ 다시 넣기
    params.put("offset", offset);
    params.put("pageSize", pageSize);
    params.put("status", null);
    return orderMapper.getProductList(params);
    }

    // ===============================================================
    // 8. 제품 전체 개수 조회
    // ===============================================================
    @Override
    public int getProductCount() {
        return orderMapper.getProductCount();
    }
}
