package com.yedam.scm.vo;

import lombok.Data;

import java.util.List;

@Data
public class SalesMasterVO {
    private String saleId;          // 판매코드 (PK)
    private Integer saleTotalAmount; // 총 판매금액(제품별 가격*주문수량들의 합
    private String saleDate;          // 판매일자
    private String salePayType;     // 결제유형 (현금, 카드 등)
    private String vendorId;        // 판매처 코드 (매장코드)

    private List<SalesDetailVO> salesDetails;
}