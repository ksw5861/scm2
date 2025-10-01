package com.yedam.scm.order.service;

import java.util.List;
import com.yedam.scm.vo.ReturnVO;
import com.yedam.scm.vo.ReturnDetailVO;

public interface ReturnService {

    // =============================================================
    // 반품 등록
    // =============================================================
    int insertReturn(ReturnVO returnVO);

    // =============================================================
    // 반품 목록 조회
    // =============================================================
    List<ReturnVO> getReturnList(String startDate,
                                 String endDate,
                                 String returnStatus,
                                 String returnId);

    // =============================================================
    // 반품 단건 조회
    // =============================================================
    ReturnVO getReturnDetail(String returnId);

    // =============================================================
    // 반품 상세 목록 조회
    // =============================================================
    List<ReturnDetailVO> getReturnDetailList(String returnId);

    // =============================================================
    // 반품 가능 주문/상세 조회
    // =============================================================
    List<ReturnVO> getReturnableOrders(String vendorId, String prodName);
    List<ReturnDetailVO> getReturnableOrderDetails(String orderId);


    // =============================================================
    // 반품 상태 변경
    // =============================================================
    boolean updateReturnStatus(String returnId, String status);

}
