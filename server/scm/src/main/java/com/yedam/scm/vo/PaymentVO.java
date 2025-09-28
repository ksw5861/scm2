package com.yedam.scm.vo;

import lombok.Data;
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
    
    private Long totalUnpaid;     // 총 미결제 금액
    private Integer overdueCount; // 연체된 건수
    private Integer upcomingCount;// 납부 예정 건수
    private Long thisMonthReturnAmount;   // 이번 달 결제 총액

    private List<PaymentDetailVO> paymentDetails; //납부등록 페이지 상단 카드 내용

    private Long completedAmount;  // 완료된 납부 총액
    private Integer completedCount; // 완료된 거래 건수
    private Integer pendingCount;   // 대기중 거래 건수 

}
