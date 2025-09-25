package com.yedam.scm.vo;

import lombok.Data;

@Data
public class PaymentDetailVO {

    private String payDetailId; 
    private String payId;       
    private String orderId;
    private int totalPrice;
    private int returnPrice;   

}
