package com.yedam.scm.order.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.SalesOrderDetailVO;
import com.yedam.scm.vo.SalesOrderVO;

/**
 * OrderMapper
 * ===========================
 * 주문 및 주문 상세 테이블 연동 Mapper
 * - MyBatis XML 매핑 파일과 1:1 매칭
 */
@Mapper
public interface OrderMapper {

    // --------------------------
    // 1. 주문 등록
    // --------------------------
    int insertOrder(SalesOrderVO orderVO);

    // --------------------------
    // 2. 주문 상세 등록
    // --------------------------
    int insertOrderDetail(SalesOrderDetailVO detailVO);

    // --------------------------
    // 3. 주문 목록 조회 (검색조건 포함)
    // --------------------------
    List<SalesOrderVO> getOrderListForView(
        @Param("startDate") String startDate,
        @Param("endDate") String endDate,
        @Param("prodName") String prodName,
        @Param("status") String status,
        @Param("orderId") String orderId
    );

    // --------------------------
    // 4. 주문 상세 조회 (order_id 기준 전체)
    // --------------------------
    List<SalesOrderDetailVO> getOrderDetailList(@Param("orderId") String orderId);

    // --------------------------
    // 5. 주문 상세 조회 (개별)
    // --------------------------
    // List<SalesOrderDetailVO> getOrderDetails(String orderId);

    // --------------------------
    // 6. 지점 대시보드
    // --------------------------
    Object getBranchDashData();

    // --------------------------
    // 7. 제품 목록 조회 (모달용)
    // --------------------------
    List<ProductVO> getProductList(Map<String, Object> params);

    // --------------------------
    // 8. 제품 전체 개수 조회
    // --------------------------
    int getProductCount();

    

}
