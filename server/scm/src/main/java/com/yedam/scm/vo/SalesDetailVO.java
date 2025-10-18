package com.yedam.scm.vo;

import lombok.Data;

@Data
public class SalesDetailVO {
    private String sdetailId;       // 판매상세코드 (PK)
    private String saleId;          // 판매코드 (FK)
    private String saleProdId;      // 제품코드 (FK)
    private String saleProdName;    // 제품명
    private Integer prodUnitPrice;  // 판매단가(본사동일)
    private Integer saleQty;        // 판매수량
    private Integer saleTotalPrice; // 수량*판매가
    private Integer saleMargin;      // 판매 당시 마진율
    private Integer saleProdPrice;  // 실제 판매단가 (마진 적용된 금액)
}
