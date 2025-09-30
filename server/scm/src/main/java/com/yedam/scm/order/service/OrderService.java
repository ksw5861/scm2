package com.yedam.scm.order.service;

import java.util.List;
import java.util.Map;
import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.SalesOrderDetailVO;
import com.yedam.scm.vo.SalesOrderVO;

/**
 * OrderService
 * 
 * 주문(판매처) 및 제품 관리 서비스 인터페이스
 */
public interface OrderService {

    // ================================
    // 1. 주문 관련 기능
    // ================================

    /**
     * 주문 등록 (마스터 + 상세)
     * @param orderVO 주문 정보
     * @return true = 성공, false = 실패
     */
    boolean insertOrder(SalesOrderVO orderVO);

    /**
     * 주문 상세 단건 등록
     * @param detailVO 주문 상세 정보
     * @return true = 성공, false = 실패
     */
    boolean insertOrderDetail(SalesOrderDetailVO detailVO);

    /**
     * 조건별 주문 목록 조회
     */
    List<SalesOrderVO> getOrderListForView(String startDate,
                                           String endDate,
                                           String prodName,
                                           String status,
                                           String orderId);

    /**
     * 특정 주문의 상세 내역 조회
     */
    List<SalesOrderDetailVO> getOrderDetailList(String orderId);

    /**
     * 지점 대시보드 데이터 조회
     */
    Map<String, Object> getBranchDashData();

    // ================================
    // 2. 제품 관련 기능
    // ================================

    /**
     * 제품 목록 조회 (모달용)
     */
    List<ProductVO> getProductList(String prodName, int offset, int limit);

    /**
     * 전체 제품 개수 조회
     */
    int getProductCount();

    /**
     * 주문 상태 변경
     * @param orderId 주문 ID
     * @param status 변경할 상태
     * @return true = 성공, false = 실패
     */   
    boolean updateOrderStatus(String orderId, String status);    


}
