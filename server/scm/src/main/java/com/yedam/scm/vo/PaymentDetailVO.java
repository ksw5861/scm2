package com.yedam.scm.vo;

import lombok.Data;

@Data
public class PaymentDetailVO {

    private String key;
    private String field;
    private String orderId;
    private int totalPrice;

}
