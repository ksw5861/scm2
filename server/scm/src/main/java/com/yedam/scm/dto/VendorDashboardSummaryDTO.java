package com.yedam.scm.dto;

import lombok.Data;

@Data
public class VendorDashboardSummaryDTO {
    
    private int totalOrder;         // 총 주문건수
    private int totalInbound;       // 총 출고건수
    private long totalAmount;       // 총 금액
    private int beforeApprove;      // 승인대기건
    private int beforeShipOrder;    // 출고지시전

}
