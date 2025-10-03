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

    // 주문 상태 변경
    int updateOrderStatus(@Param("orderId") String orderId, @Param("status") String status);

    // 출하 등록 시 send_date 업데이트
    int updateSendDate(String orderId);

    // 출하지시 대기 주문 (처리완료)
    List<SalesOrderVO> selectPendingDeliveryOrders();

    // 출하지시 진행 주문 (배송준비중)
    List<SalesOrderVO> selectDeliveryInProgressOrders();

    // 출하지시가능 주문건 상세보기
    List<SalesOrderDetailVO> selectDeliveryDetailList(String orderId);

    // 출하지시 진행 주문 조회 (상태: 배송준비중) - 바코드찍을것들
    List<Map<String, Object>> selectReadyToDeliverSimple();
}
