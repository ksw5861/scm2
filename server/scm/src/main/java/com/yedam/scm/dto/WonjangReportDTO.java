package com.yedam.scm.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class WonjangReportDTO {
    private String companyName;     // 거래처명
    private BigDecimal totalPrice;  // 총매출
    private BigDecimal returnPrice; // 총반품
    private BigDecimal totalPayment;// 총입금
    private BigDecimal totalAr;     // 미수금
    private Integer orderCount;     // 주문건수
    private Integer unpaidCount;    // 미수건수
    private String lastOrderDate;   // 최근거래일자
}