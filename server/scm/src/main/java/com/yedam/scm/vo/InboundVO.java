package com.yedam.scm.vo;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class InboundVO {
    
    private Long inboundId;       // PK
    private String inboundNo;     // 입고번호
    private Date vendorOutDate;   // 공급처출고일자
    private String venName;       // 공급처담당자
    private String inboundStatus; // 상태
    private String vendorId;      // 공급처코드
    private String venOutNo;      // 출고번호
    private String rejMemo;       // 하차거부사유
    private String unloadEmp;     //하차담당자
    private Date unloadDate; //하차일

    List<InboundDetailVO> details;     // 입고 상세들

    ShipmentInfoVO shipmentInfoVO; //배송정보
    
    VendorVO vendorVO; //거래처
}


