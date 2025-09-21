package com.yedam.scm.order.service;

import java.util.List;

import com.yedam.scm.vo.SalesOrderDetailVO;
import com.yedam.scm.vo.SalesOrderVO;

public interface OrderService {
    int insertOrder(SalesOrderVO orderVO);
    int insertOrderDetail(SalesOrderDetailVO detailVO);
    List<SalesOrderVO> getOrderList(String startDate, String endDate, String status);
    List<SalesOrderDetailVO> getOrderDetails(String orderId);

    List<SalesOrderVO> getOrderListForView(String startDate, String endDate, String status);
    Object getBranchDashData();

    
}
