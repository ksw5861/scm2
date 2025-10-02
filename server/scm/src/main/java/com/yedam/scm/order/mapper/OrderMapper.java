package com.yedam.scm.order.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.SalesOrderDetailVO;
import com.yedam.scm.vo.SalesOrderVO;

@Mapper
public interface OrderMapper {

    // =============================================================
    // 주문 등록
    // =============================================================
    int insertOrder(SalesOrderVO orderVO);

    // =============================================================
    // 주문 상세 등록
    // =============================================================
    int insertOrderDetail(SalesOrderDetailVO detailVO);

    // =============================================================
    // 주문 목록 조회 (검색조건 포함)
    // =============================================================
    List<SalesOrderVO> getOrderListForView(
        @Param("startDate") String startDate,
        @Param("endDate") String endDate,
        @Param("prodName") String prodName,
        @Param("status") String status,
        @Param("orderId") String orderId
    );

    // =============================================================
    // 주문 상세 조회
    // =============================================================
    List<SalesOrderDetailVO> getOrderDetailList(@Param("orderId") String orderId);

    // =============================================================
    // 지점 대시보드 데이터
    // =============================================================
    Map<String, Object> getBranchDashData();

    // =============================================================
    // 제품 목록 조회 (모달용)
    // =============================================================
    List<ProductVO> getProductList(Map<String, Object> params);

    // =============================================================
    // 제품 전체 개수 조회
    // =============================================================
    int getProductCount();

    // =============================================================
    // 주문 상태 변경
    // =============================================================
    int updateStatus(@Param("orderId") String orderId,
                     @Param("status") String status);

}
