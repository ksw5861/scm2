package com.yedam.scm.vo;

import java.util.Date;

import lombok.Data;

@Data
public class InboundDetailVO {
    
    private Long inboundDetId;   // INBOUND_DET_ID
    private Long inQty;          // IN_QTY
    private String status;       // STATUS
    private Long inboundId;      // INBOUND_ID (FK to inbound)
    private String result;       // RESULT (검사결과)
    private String img;          // IMG (사진 이미지 경로/파일명)
    private Long purId;          // PUR_ID (발주 ID)
    private Date inspectDate;    // INSPECT_DATE (검수일)
    private Long inTotalQty;     // IN_TOTAL_QTY (누적입고수량)
    private String matId;        // MAT_ID (자재코드)
    private String empName;      // EMP_NAME (입고담당자)
    private String vendorId;     // VENDOR_ID (공급처코드)
    private Long outQty;         // OUT_QTY (공급처출고수량)
    private Long purStatusId;    //출고지시번호 최종출고 상태변경(분할출고로 인해 필요) => 출고완료시 'Y' 해당 아이디 찾기용

    InboundLogVO inboundLogVO;
}


