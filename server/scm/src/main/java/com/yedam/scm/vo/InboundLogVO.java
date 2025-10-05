package com.yedam.scm.vo;



import java.util.Date;

import lombok.Data;

@Data
public class InboundLogVO {
    
    private Long inLogId;         // IN_LOG_ID - 분할입고이력 고유ID
    private Long inboundDetId;    // INBOUND_DET_ID - 품목상세 ID (FK)
    private Long logInQty;        // LOG_IN_QTY - 입고수량
    private String logInboundStatus;  // LOG_INBOUND_STATUS
    private Date reDate;          // RE_DATE - 등록일자
    private String logName;       // LOG_NAME - 담당자명
    private String logResult;     // LOG_RESULT - 검수결과 (합격/불합격)
    private String logImg;        // LOG_IMG - 첨부 이미지 (파일명 or 경로)
    private Long logRejQty;       // LOG_REJ_QTY - 불량수량
    private String logMemo;       // LOG_MEMO - 비고/메모
    private Date logExpDate;  //LOG_EXP_DATE
}