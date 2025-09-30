package com.yedam.scm.vo;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class ReturnVO {
    private String returnId;       // 반품번호
    private String returnDate;       // 반품일자
    private String vendorId;       // 거래처 ID
    private Long returnPrice;      // 반품 총액
    private String returnStatus;         // 반품 상태 (예: 반품접수, 반품완료)
    
    // =========== 주문(반품) 조회용 필드 ===========
    private String orderId;
    private Date orderDate;
    private Date deliveryDate;
    private Long totalPrice;
    private String prodName; // 대표 상품명 (필요 시)

    // =========== 상세 리스트 ===========
    private List<ReturnDetailVO> details;  // 반품 상세 정보


    // 0930 동원 추가

    private String companyName; // 판매처명


}

