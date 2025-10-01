package com.yedam.scm.vo;

import java.util.Date;

import lombok.Data;

@Data
public class InboundVO {
    
    private Long inboundId;       // INBOUND_ID (PK)
    private String inboundNo;     // INBOUND_NO (입고번호)
    private Date vendorOutDate;   // VANDER_OUT_DATE (공급처출고일자)
    private String venName;       // VAN_NAME (공급처담당자)
    private String inboundStatus; // INBOUND_STATUS (상태: 입고대기, 부분입고, 입고완료 등)
    private String vendorId;      // VANDER_ID (공급처코드)
    private String venOutNo;      // VAN_OUT_NO (출고번호)
    private Integer count;        // COUNT (품목수량)
    private String rejMemo;       //REJ_MEMO (거부사유)

    InboundDetailVO inboundDetailVO;
}


