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
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    // ===============================================================
    // 1. 주문 등록 (마스터 + 상세)
    // ===============================================================
    @Transactional
    @Override
    public boolean insertOrder(SalesOrderVO orderVO) {

        // 1) 주문 마스터 저장
        int masterResult = orderMapper.insertOrder(orderVO);

        // 2) 마스터 저장 성공 시 주문 상세 저장
        if (masterResult > 0 && orderVO.getDetails() != null && !orderVO.getDetails().isEmpty()) {
            for (SalesOrderDetailVO detail : orderVO.getDetails()) {
                // 주문번호 세팅
                detail.setOrderId(orderVO.getOrderId());

                // DB에 그대로 저장 (totalUnitPrice는 DB에서 계산 가능)
                int detailResult = orderMapper.insertOrderDetail(detail);

                // 하나라도 실패하면 롤백
                if (detailResult <= 0) {
                    throw new RuntimeException("주문 상세 저장 실패");
                }
            }
            return true;
        }

        return masterResult > 0;
    }

    // ===============================================================
    // 2. 주문 상세 단건 등록
    // ===============================================================
    @Override
    public boolean insertOrderDetail(SalesOrderDetailVO detailVO) {
        return orderMapper.insertOrderDetail(detailVO) > 0;
    }

    // ===============================================================
    // 3. 조건별 주문 목록 조회
    // ===============================================================
    @Override
    public List<SalesOrderVO> getOrderListForView(String startDate,
                                                  String endDate,
                                                  String prodName,
                                                  String status,
                                                  String orderId) {
        return orderMapper.getOrderListForView(startDate, endDate, prodName, status, orderId);
    }

    // ===============================================================
    // 4. 특정 주문의 상세 내역 조회
    // ===============================================================
    @Override
    public List<SalesOrderDetailVO> getOrderDetailList(String orderId) {
        return orderMapper.getOrderDetailList(orderId);
    }

    // ===============================================================
    // 5. 지점 대시보드 데이터 조회
    // ===============================================================
    @Override
    public Map<String, Object> getBranchDashData() {
        return orderMapper.getBranchDashData();
    }

    // ===============================================================
    // 6. 제품 목록 조회 (모달용)
    // ===============================================================
    @Override
    public List<ProductVO> getProductList(String prodName, int offset, int pageSize) {
        Map<String, Object> params = new HashMap<>();
        params.put("prodName", prodName);
        params.put("offset", offset);
        params.put("pageSize", pageSize);
        params.put("status", null); // 상태값 필터링이 필요 없다면 null
        return orderMapper.getProductList(params);
    }

    // ===============================================================
    // 7. 제품 전체 개수 조회
    // ===============================================================
    @Override
    public int getProductCount() {
        return orderMapper.getProductCount();
    }
}
