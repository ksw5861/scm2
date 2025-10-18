package com.yedam.scm.vo;

import lombok.Data;

@Data
public class PaymentDetailVO {

    private String payDetailId; 
    private String payId;       
    private String orderId;
    private int totalPrice;
    private int returnPrice;   
    private String dataType;


    private String returnId;
   


    //1018 추가
    private int allocAmount;   // 입금 상세 금액
}
