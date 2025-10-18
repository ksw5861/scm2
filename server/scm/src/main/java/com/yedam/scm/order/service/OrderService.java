package com.yedam.scm.order.service;

import java.util.List;
import java.util.Map;
import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.SalesOrderDetailVO;
import com.yedam.scm.vo.SalesOrderVO;

public interface OrderService {

    boolean insertOrder(SalesOrderVO orderVO);

    boolean insertOrderDetail(SalesOrderDetailVO detailVO);

    List<SalesOrderVO> getOrderListForView(
        String vendorId,
        String startDate,
        String endDate,
        String prodName,
        String status,
        String orderId
    );

    List<SalesOrderDetailVO> getOrderDetailList(String orderId);

    Map<String, Object> getBranchDashData();

    List<ProductVO> getProductList(Map<String, Object> params);

    int getProductCount();

    boolean updateOrderStatus(String orderId, String status);

    int deleteOrderWithDetails(String orderId);

    List<SalesOrderDetailVO> getOrderDetails(String orderId);
}

