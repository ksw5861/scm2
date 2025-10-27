package com.yedam.scm.order.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.yedam.scm.vo.SalesOrderVO;
import com.yedam.scm.vo.SalesOrderDetailVO;

@Mapper
public interface GoDelMapper {

    // 출하지시 등록 (shipId는 XML에서 selectKey로 생성)
    int createDeliveryInstruction(SalesOrderVO order);
    //출하지시 등록시 제품명 + 주문수량 받아오기(salesOrderDetail테이블에 들어았는 값으로 가지고 옴)
   List<SalesOrderDetailVO> getProdandQTY(String orderId);
    //받아온 제품코드와 수량으로 재고차감
    void callProcStockReduction(@Param("prodId") String prodId, @Param("orderQty") Long orderQty);
    
    // 출고완료 주문건
    List<SalesOrderVO> selectShippedOrders();

    // 주문 상태 변경
    int updateOrderStatus(@Param("shipId") String shipId, @Param("status") String status);

    // 출하지시 대기 주문 (처리완료)
    List<SalesOrderVO> selectPendingDeliveryOrders();

    // 출하지시 진행 주문 (배송준비중)
    List<SalesOrderVO> selectDeliveryInProgressOrders();

    // 출하지시가능 주문건 상세보기
    List<SalesOrderDetailVO> selectDeliveryDetailList(String orderId);

    // 출하지시 진행 주문 조회 (상태: 배송준비중) - 바코드찍을것들
    List<Map<String, Object>> selectReadyToDeliverSimple();
}
