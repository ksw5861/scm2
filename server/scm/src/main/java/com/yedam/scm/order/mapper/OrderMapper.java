package com.yedam.scm.order.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.yedam.scm.vo.SalesOrderDetailVO;
import com.yedam.scm.vo.SalesOrderVO;

@Mapper
public interface OrderMapper {
  
  // 주문 등록
    int insertOrder(SalesOrderVO orderVO);

    // 주문 상세 등록
    int insertOrderDetail(SalesOrderDetailVO detailVO);

    // 주문 목록 조회
    List<SalesOrderVO> getOrderList(String startDate, String endDate, String status);

    // 특정 주문 상세 내역 조회
    List<SalesOrderDetailVO> getOrderDetails(String orderId);


    // 주문 목록 조회
    List<SalesOrderVO> getOrderListForView(
        @Param("startDate") String startDate,
        @Param("endDate") String endDate,
        @Param("status") String status
    );


     // 지점 대시보드
    Object getBranchDashData();

    
}
