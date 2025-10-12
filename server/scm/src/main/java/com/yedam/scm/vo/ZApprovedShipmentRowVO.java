package com.yedam.scm.vo;



import java.util.Date;

import lombok.Data;

@Data
public class ZApprovedShipmentRowVO {
    private Long purId;
    private Date expectDate;
    private Long outQty;
    private String logShipOrderNo;
    private String matId;
    private String matName;
    private String stockUnit;
    private String empName;
    private String vendorId;
    private Long purStatusId;
    private String warehouseName;
}
