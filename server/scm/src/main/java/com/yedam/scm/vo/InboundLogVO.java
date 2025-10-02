package com.yedam.scm.vo;

import java.sql.Date;

import lombok.Data;

@Data
public class InboundLogVO {
    
    private Long inLogId;        // IN_LOG_ID
    private Long inboundDetId;   // INBOUND_DET_ID
    private Long logInQty;       // LOG_IN_QTY (분할입고용)
    private String logMatStatus; // LOG_MAT_STATUS (당시상태값)
    private Date reDate;         // RE_DATE (이력등록된sysdate)
    private String logName;      // LOG_NAME (이력기록된 당시 담당자)
    private Date logInspectDate; // LOG_INSPECT_DATE (분할입고용 입고날짜)
    private String logResult;    // LOG_RESULT (분할입고용 입고결과)
    private String logImg;       // LOG_IMG (분할입고용 이미지)

}