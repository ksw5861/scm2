package com.yedam.scm.vo;

import lombok.Data;

@Data
public class ReturnDetailVO {

    private String rdetailId;      // 반품 상세 PK
    private String returnId;       // 반품 마스터 FK
    private String prodId;         // 상품 코드
    private String prodName;       // 상품명
    private String spec;           // 규격
    private String unit;           // 단위
    private int returnQty;         // 반품 수량
    private int prodUnitPrice;     // 반품 당시 단가 (스냅샷)
    private int amt;               // 금액 (returnQty * prodUnitPrice)
    private String returnWhy;      // 반품 사유
    private String rdetailStatus;  // 반품 상세 상태 (REQ, APPROVE, COMPLETE)
}
