package com.yedam.scm.vo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
public class SalesOrderVO {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String orderId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Date orderDate;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Long totalPrice;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String vendorId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Date deliveryDate;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String payStatus;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Date sendDate;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Date paydueDate;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Long returnPrice;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String dataType; // 'ORDER' or 'RETURN'

    @JsonInclude(JsonInclude.Include.NON_NULL)
    BigDecimal finalAmount;

    private Long remainCredit; // 여신 잔액
    private Long creditLimit; // 여신 한도
    private BigDecimal nextDueAmount; // 다음 결제기한 기준 미수금 합계
    private String nextDueDate; // 다음 결제기한 (예: "2025-02-15")

    // 추가된 대표 제품명 필드
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String prodName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String shipId;

    private String failReason;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<SalesOrderDetailVO> details;

    // Getter / Setter
    public String getProdName() {
        return prodName;
    }

    public void setProdName(String prodName) {
        this.prodName = prodName;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String prodId;

    // 동원 추가
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String companyName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String prodStatus;

    // 거래처원장용 집계 필드
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String vendorType; // 거래처구분 (판매처 / 공급처)

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal totalSales; // 총매출

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal totalReturn; // 총반품

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal totalPayment; // 총입금

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal totalAr; // 미수금

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal totalAp; // 미지급금

    // 마지막 거래 일자
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date lastOrderDate;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal prevTotalPrice; // 이전 금액

    // 1018 추가
    private Integer orderCount; // 주문건수
    private Integer unpaidCount; // 미수건수

}
