package com.yedam.scm.vo;

import lombok.Data;

@Data
public class SalesDetailVO {
    private String sdetailId;       // 판매상세코드 (PK)
    private String saleId;          // 판매코드 (FK)
    private String saleProdId;      // 제품코드 (FK)
    private String saleProdName;    // 제품명
    private Integer saleProdPrice;  // 판매단가
    private Integer saleQty;        // 판매수량
    private Double saleMargin;      // 판매 당시 마진율
    private Integer saleUnitPrice;  // 실제 판매단가 (마진 적용된 금액)
}
