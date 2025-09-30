package com.yedam.scm.order.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.yedam.scm.vo.ReturnVO;
import com.yedam.scm.vo.ReturnDetailVO;

@Mapper
public interface ReturnMapper {

    // 1. 반품 등록
    int insertReturnOrder(ReturnVO returnVO);               
    int insertReturnDetail(ReturnDetailVO returnDetailVO);  

    // 2. 반품 목록 조회
    List<ReturnVO> getReturnList(
        @Param("startDate") String startDate,
        @Param("endDate") String endDate,
        @Param("returnStatus") String returnStatus,
        @Param("prodName") String prodName,
        @Param("returnId") String returnId
    );

    // 3. 반품 단건 조회
    ReturnVO getReturnDetail(String returnId);              
    List<ReturnDetailVO> getReturnDetailList(String returnId);

    // 4. 모달 - 반품 가능 주문 / 상세 조회
    List<ReturnVO> getReturnableOrders(
        @Param("vendorId") String vendorId,
        @Param("prodName") String prodName
    );
    List<ReturnDetailVO> getReturnableOrderDetails(@Param("orderId") String orderId);


    // 5. 반품 상태 변경
     int updateStatus(@Param("returnId") String returnId,
                     @Param("status") String status);
}
