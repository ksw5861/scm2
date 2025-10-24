package com.yedam.scm.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class PaymentVO {

    private String payId;
    private String payRmk;
    private int payAmount;
    private String vendorId;
    private Date payDate;
    private String payType;
    
    private Long totalUnpaid;     // 총 미결제 금액 ---나중에 보고 삭제하기
    private Integer overdueCount; // 연체된 건수
    private Integer upcomingCount;// 납부 예정 건수
    private Long thisMonthReturnAmount;   // 이번 달 결제 총액
  
    private List<PaymentDetailVO> paymentDetails; //납부등록 페이지 상단 카드 내용

    private Long completedAmount;  // 완료된 납부 총액
    private Integer completedCount; // 완료된 거래 건수
    private Integer pendingCount;   // 대기중 거래 건수 

    private Long outstandingAmount;  // 미수금(결제전:납부조회페이지 플로우형식)
    private Long finalBalance;       // 최종 잔액(결제후:납부조회페이지 플로우형식)
    private Long creditBalance;      // 여신 잔액 (납부조회페이지 플로우형식)

    
    private String impUid;        // 아임포트 결제번호
    private String merchantUid;   // 가맹점 주문번호
   // 총 결제 금액
    private String memo;          // 메모

    private Long creditLimit;    // 당시 원본 여신한도//납부기록 로그용
    private Long payPreAr;       // 결제 전 미수금
    private Long payAfterAr;     // 결제 후 미수금
    private Long payPreLimit;    // 결제 전 잔여 여신한도
    private Long payAfterLimit;  // 결제 후 잔여 여신한도

  

    private List<OrderItem> orders; // 주문 리스트 [{orderId, totalPrice}...]

    // --- Getter / Setter ---
    public String getImpUid() { return impUid; }
    public void setImpUid(String impUid) { this.impUid = impUid; }

    public String getMerchantUid() { return merchantUid; }
    public void setMerchantUid(String merchantUid) { this.merchantUid = merchantUid; }

    public Integer getTotalAmount() { return payAmount; }
    public void setTotalAmount(Integer totalAmount) { this.payAmount = totalAmount; }

    public String getPayType() { return payType; }
    public void setPayType(String payType) { this.payType = payType; }

    public Date getPayDate() { return payDate; }
    public void setPayDate(Date payDate) { this.payDate = payDate; }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }

    public String getVendorId() { return vendorId; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }

    public List<OrderItem> getOrders() { return orders; }
    public void setOrders(List<OrderItem> orders) { this.orders = orders; }

    // 주문 항목 내부 클래스
    public static class OrderItem {
        private String orderId;
        private Integer totalPrice;

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }

        public Integer getTotalPrice() { return totalPrice; }
        public void setTotalPrice(Integer totalPrice) { this.totalPrice = totalPrice; }
    }



    
}
