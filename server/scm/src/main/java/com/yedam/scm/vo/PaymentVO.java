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
    private List<PaymentDetailVO> paymentDetails;

}
