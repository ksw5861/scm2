package com.yedam.scm.vo;

import java.util.Date;

import lombok.Data;

@Data
public class MrpDetailVO {

    private Long mrpDetId;          // MRP_DET_ID
    private Long plId;              // PL_ID
    private String matId;           // MAT_ID
    private Double needsWeight;        //NEEDS_WEIGHT
    private Double currentStock;    // CURRENT_STOCK
    private Double onOrderQty;      // ON_ORDER_QTY
    private Double safeStock;       // SAFE_STOCK
    private Double shortageQty;     // SHORTAGE_QTY
    private String unit;            // UNIT
    private Integer leadTime;       // LEAD_TIME
    private Date calDate;           // CAL_DATE
    private String empName;         // EMP_NAME
    private String status;          // STATUS

    MaterialVO materialVO;
}

 

