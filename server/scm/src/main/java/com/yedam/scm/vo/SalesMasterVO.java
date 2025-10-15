package com.yedam.scm.vo;

import lombok.Data;
import java.util.Date;

@Data
public class SalesMasterVO {
    private String saleId;          // 판매코드 (PK)
    private String sdetailId;       // 판매상세코드 (FK)
    private Integer saleTotalPrice; // 총 판매금액
    private Date saleDate;          // 판매일자
    private String salePayType;     // 결제유형 (현금, 카드 등)
    private String vendorId;        // 판매처 코드 (매장코드)
}