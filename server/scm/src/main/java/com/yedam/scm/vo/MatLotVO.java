package com.yedam.scm.vo;

import java.util.Date;

import lombok.Data;

@Data
public class MatLotVO {
    
    private Long lotId;
    private String matId;
    private String lotNo;
    private Date regDate;
    private Integer initQty;
    private Integer currQty;
    private Date expDate;
    private String warehouse;
    private String empName;
    private String lotStatus;
    private Long inboundDetId;
    private Double initWeight;
    private Double currWeight;

    private MaterialVO materialVO;
    
}
