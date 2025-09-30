package com.yedam.scm.vo;

import java.sql.Date;

import lombok.Data;

@Data
public class InboundLogVO {
    
    private Long inLogId;        // IN_LOG_ID (PK)
    private Long inboundDetId;   // INBOUND_DET_ID (입고상세아이디, FK)
    private Long inQty;          // IN_QTY (입고수량)
    private String status;       // STATUS (상태: 입고대기, 부분입고, 입고완료, 반품 등)
    private Date reDate;         // RE_DATE (상태 변경일시)
    private String name;      // EMP_NAME (담당자)

}
