package com.yedam.scm.order.service;

import java.util.List;

import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.SalesOrderDetailVO;
import com.yedam.scm.vo.SalesOrderVO;

/**
 * OrderService
 * 
 * 주문(판매처) 및 제품 관리 서비스 인터페이스
 * Controller ↔ Service ↔ Mapper 계층 중
 * Service 계층의 역할을 정의한다.
 */
public interface OrderService {

    // ======================================================
    // 1. 주문(판매처) 관련 기능
    // ======================================================

    /**
     * 주문 마스터 & 상세 등록
     * 
     * @param orderVO 주문 정보 (마스터 + 상세 내역 포함)
     * @return 등록 성공 시 1, 실패 시 0
     */
    int insertOrder(SalesOrderVO orderVO);

    /**
     * 주문 상세 단건 등록
     * 
     * @param detailVO 주문 상세 정보
     * @return 등록 성공 시 1, 실패 시 0
     */
    int insertOrderDetail(SalesOrderDetailVO detailVO);

    /**
     * 특정 주문의 모든 상세 내역 조회
     * 
     * @param orderId 주문번호
     * @return 주문 상세 리스트
     */
    // List<SalesOrderDetailVO> getOrderDetails(String orderId);

    /**
     * 조건별 주문 목록 조회
     * 
     * @param startDate 시작일 (yyyy-MM-dd)
     * @param endDate   종료일 (yyyy-MM-dd)
     * @param prodName  제품명 (검색어)
     * @param status    주문 상태
     * @param orderId   주문번호 (부분 검색 가능)
     * @return 주문 목록 리스트
     */
    List<SalesOrderVO> getOrderListForView(
        String startDate,
        String endDate,
        String prodName,
        String status,
        String orderId
    );

    /**
     * 지점 대시보드 데이터 조회
     * 
     * @return 총 주문 건수, 매출 합계 등 지점 현황 데이터
     */
    Object getBranchDashData();

    /**
     * 특정 주문의 상세 내역 조회 (반품, 상세화면 등에서 사용)
     * 
     * @param orderId 주문번호
     * @return 주문 상세 리스트
     */
    List<SalesOrderDetailVO> getOrderDetailList(String orderId);

    // ======================================================
    // 2. 제품(상품) 관련 기능
    // ======================================================

    /**
     * 제품 목록 조회 (모달용, 페이징 적용)
     * 
     * @param offset 시작 위치
     * @param limit  조회할 개수
     * @return 제품 목록 리스트
     */
    List<ProductVO> getProductList(String prodName, int offset, int limit);

    /**
     * 전체 제품 개수 조회
     * 
     * @return 제품 총 개수
     */
    int getProductCount();

}
