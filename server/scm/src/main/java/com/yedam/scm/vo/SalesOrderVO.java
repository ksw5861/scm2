package com.yedam.scm.vo;

import java.util.Date;

import lombok.Data;

@Data
public class SalesOrderVO {

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
  


}
