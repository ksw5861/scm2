package com.yedam.scm.vo;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class SalesOrderVO {

  String orderId;
  Date orderDate;
  Long totalPrice;
  String vendorId;
  String status;
  Date deliveryDate;
  String payStatus;
  Date sendDate;
  Date paydueDate;
  Long returnPrice;
  
  // 추가된 대표 제품명 필드
    private String prodName;

    // Getter / Setter
    public String getProdName() {
        return prodName;
    }

    public void setProdName(String prodName) {
        this.prodName = prodName;
    }

    private List<SalesOrderDetailVO> details;

}
