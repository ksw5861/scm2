package com.yedam.scm.vo;

import java.util.Date;

import lombok.Data;

@Data
public class SalesOrderDetailVO {

  private String odetailId;
  private String orderId;     // 주문 번호 (SalesOrderVO와 연관)
  private String prodId;
  private String prodName;
  private String spec;
  private String unit;
  private Long orderQty;

  // ✅ PROD_UNIT_PRICE 사용
  private Long prodUnitPrice;
  private Long totalUnitPrice;    // 합계 (prodUnitPrice * orderQty)
  private String prodStatus;

   private int shipQty;
      private String shipId;   // 출하지시 헤더ID

}
