package com.yedam.scm.vo;

import java.util.Date;

import lombok.Data;

@Data
public class PurchaseMatVO {
    
    private Long purId;
    private String matId;
    private Long reqQty;
    private Long outTotalQty;
    private String purMatStatus;
    private String resonComm;
    private Long mrpDetId;
    private String vendorId;
    private String empName;
    private Date regDate;
    private String purNo;
    private Long total;
    private Date dueDate;

    MaterialVO materialVO;
    PurStatusLogVO purStatusLogVO;
}

