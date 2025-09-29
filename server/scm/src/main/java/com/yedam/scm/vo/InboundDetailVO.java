package com.yedam.scm.vo;

import java.util.Date;

import lombok.Data;

@Data
public class InboundDetailVO {

    private Long inboundDetId;   // INBOUND_DET_ID
    private Long qty;         // QTY
    private String status;       // STATUS
    private Long inboundId;      // INBOUND_ID
    private String result;       // RESULT
    private String img;          // IMG
    private Long purDetId;       // PUR_DET_ID
    private Date inspectDate; // INSPECT_DATE
    private Long inTotalQty;  // IN_TOTAL_QTY
    private String matId;        // MAT_ID
    
}
