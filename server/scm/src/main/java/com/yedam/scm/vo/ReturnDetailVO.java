package com.yedam.scm.vo;

import lombok.Data;

@Data
public class ReturnDetailVO {

    // ====== 기본 키 및 관계 ======
    private String rdetailId;      // 반품 상세 PK
    private String returnId;       // 반품 마스터 FK (ReturnVO와 1:N 관계)

    // ====== 상품 정보 ======
    private String prodId;         // 상품 코드
    private String prodName;       // 상품명
    private String spec;           // 규격
    private String unit;           // 단위

    // ====== 수량 및 금액 ======
    private int returnQty;         // 반품 수량
    private int prodUnitPrice;     // 반품 당시 단가 (스냅샷)
    private int amt;               // 금액 (returnQty * prodUnitPrice)

    // ====== 반품 상태 ======
    private String returnWhy;      // 반품 사유
    private String rdetailStatus;  // 반품 상세 상태 (REQ: 요청, APPROVE: 승인, COMPLETE: 완료)

    // ====== 주문 조회 시 필요한 추가 필드 ======
    private String orderId;        // 관련된 주문 번호
    private int orderQty;          // 기존 주문 수량
    private String prodStatus;     // 상품 상태 (배송완료 등)


    private Integer returnedQty;   // 이미 반품된 수량 (대기 포함)
    private Integer remainQty;     // 남은 반품 가능 수량 = orderQty - returnedQty



    // 1011 동원 추가// 
     private String companyName; 




}
