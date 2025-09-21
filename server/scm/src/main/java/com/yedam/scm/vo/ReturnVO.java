package com.yedam.scm.vo;

import java.util.Date;

import lombok.Data;

@Data
public class ReturnVO {

    private String returnId;
    private String odetailId;
    private int returnQty;
    private String returnWhy;
    private int returnPrice;
    private Date returnDate;
    private String returnStatus;
    
}