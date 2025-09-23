package com.yedam.scm.vo;

import java.util.Date;

import lombok.Data;

@Data
public class SalesOrderDetailVO {

  String odetailId;
  String prodId;
  String prodName;
  String spec;
  String unit;
  Long orderQty;
  Long prodPrice;
  String prodStatus;

  String orderId; // 주문 번호 (SalesOrderVO와 연관)
  Long totalPrice; // 합계 (prodPrice * orderQty)
    


}
