package com.yedam.scm.vo;

import java.math.BigDecimal;
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
    String dataType; // 'ORDER' or 'RETURN'
    BigDecimal finalAmount;
    // 추가된 대표 제품명 필드
    private String prodName;
    private String shipId;
    private List<SalesOrderDetailVO> details;

    // Getter / Setter
    public String getProdName() {
        return prodName;
    }

    public void setProdName(String prodName) {
        this.prodName = prodName;
    }

    private String prodId;

    // 동원 추가

    private String companyName;
    private String prodStatus;

    // 거래처원장용 집계 필드
    private String vendorType; // 거래처구분 (판매처 / 공급처)
    private java.math.BigDecimal totalSales; // 총매출
    private java.math.BigDecimal totalReturn; // 총반품
    private java.math.BigDecimal totalPayment; // 총입금
    private java.math.BigDecimal totalAr; // 미수금
    private java.math.BigDecimal totalAp; // 미지급금

}
