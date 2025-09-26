package com.yedam.scm.vo;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class ReturnVO {

    private String returnId;       // 반품 마스터 PK
    private String orderId;        // 반품이 속한 주문 번호
    private String vendorId;       // 가맹점 코드
    private String returnDate;       // 반품 요청 일자
    private int returnPrice;          // 반품 총액
    private String returnStatus;         // 반품 상태 (REQ, APPROVE, COMPLETE)

    // ✅ 반품 상세 목록
    private List<ReturnDetailVO> details;
}
