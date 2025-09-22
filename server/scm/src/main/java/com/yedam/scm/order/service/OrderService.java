package com.yedam.scm.order.service;

import java.util.List;

import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.SalesOrderDetailVO;
import com.yedam.scm.vo.SalesOrderVO;

public interface OrderService {
    // =============================
    // 주문 관련
    // =============================
    int insertOrder(SalesOrderVO orderVO);
    int insertOrderDetail(SalesOrderDetailVO detailVO);

    List<SalesOrderDetailVO> getOrderDetails(String orderId);

    List<SalesOrderVO> getOrderListForView(
        String startDate,
        String endDate,
        String prodName,
        String status,
        String orderId
    );

    Object getBranchDashData();

    // 주문 상세 조회
    List<SalesOrderVO> getOrderDetailList(String orderId);

    // =============================
    // 제품 관련 (모달 조회용)
    // =============================
    List<ProductVO> getProductList(int offset, int limit);

    int getProductCount();
}
