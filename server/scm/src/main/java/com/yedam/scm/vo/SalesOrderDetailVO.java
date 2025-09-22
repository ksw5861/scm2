package com.yedam.scm.vo;

import java.util.Date;

import lombok.Data;

@Data
public class SalesOrderDetailVO {

  String odetailId;
  String prodId;
  String prodName;
  String prodSpec;
  String prodUnit;
  Long orderQty;
  Long prodPrice;
  String prodStatus;
    


}
