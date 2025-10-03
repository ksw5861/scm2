package com.yedam.scm.order.service;

import java.util.List;
import java.util.Map;

import com.yedam.scm.vo.SalesOrderVO;
import com.yedam.scm.vo.SalesOrderDetailVO;

public interface GoDelService {

    /**
     * 출하지시 등록 (주문 상태: 처리완료 → 배송준비중)
     * @param orderIds 출하지시에 포함할 주문 ID 리스트
     * @return 성공 여부
     */
    boolean createDeliveryInstruction(List<String> orderIds);

    /**
     * 출하 등록 (주문 상태: 배송준비중 → 출고완료)
     * @param orderIds 출하 등록할 주문 ID 리스트
     * @return 성공 여부
     */
    boolean registerShipment(String shipId); // 배송준비중 → 출고완료

    List<SalesOrderVO> selectShippedOrders();        // 출고완료 목록

    boolean completeDelivery(String shipId); // 출고완료 → 배송완료

    /**
     * 출하지시 대기 중인 주문 조회 (상태: 처리완료)
     */
    List<SalesOrderVO> selectPendingDeliveryOrders();

    /**
     * 출하지시 진행 중인 주문 조회 (상태: 배송준비중)
     */
    List<SalesOrderVO> getDeliveryInProgressOrders();

    /**
     * 출하지시 가능한 주문목록 상세보기
     */
    List<SalesOrderDetailVO> getDeliveryDetailList(String orderId);

    /**
     * 출하지시 진행 주문 조회 (상태: 배송준비중) - 바코드찍을것들
     */ 
    List<Map<String, Object>> selectReadyToDeliverSimple();



}
