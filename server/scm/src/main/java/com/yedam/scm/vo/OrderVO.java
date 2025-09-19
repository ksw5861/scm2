package com.yedam.scm.vo;

import java.util.Date;

import lombok.Data;

@Data
public class OrderVO {

  String orderId;
  Date orderDate;
  Long totalPrice;
  String vendorId;
  String status;
  Date deleveryDate;
  String payStatus;
  Date sendDate;
  Date paydueDate;
  Long returnPrice;
  String odetailId;
  String prodId;
  String prodName;
  String prodSpec;
  String prodUnit;
  Long orderQty;
  Long prodPrice;
  String prodStatus;
    


}
