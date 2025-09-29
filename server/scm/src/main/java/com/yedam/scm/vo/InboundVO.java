package com.yedam.scm.vo;

import java.util.Date;

import lombok.Data;

@Data
public class InboundVO {
    
    private Long inboundId;        // INBOUND_ID
    private String inboundNo;      // INBOUND_NO
    private Date vanderOutDate; // VANDER_OUT_DATE
    private String vanName;        // VAN_NAME
    private String inboundStatus;  // INBOUND_STATUS
    private String vanderId;       // VANDER_ID
    private String vanOutNo;       // VAN_OUT_NO
    private Integer count;         // COUNT
    private String empName;        // EMP_NAME
    private Date inDate;  // IN_DATE

    InboundDetailVO inboundDetailVO;

}
