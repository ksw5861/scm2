package com.yedam.scm.vo;


import java.util.Date;

import lombok.Data;

@Data
public class PurStatusLogVO {

    private Long purStatusId;    // PUR_STATUS_ID
    private Date reDate;         // RE_DATE   
    private String logName;         // NAME
    private String logPurMatStatus; // PUR_MAT_STATUS
    private Long logPurId;          // PUR_ID
    private Long logSupOutQty;      //공급처 출고수량 (for 부분출고)
    private String logVendorId;
    private String logResonComm; //반려사유(공급처)
    //부분출고처리때문에 컬럼추가 
    private Date logExpectDate;
    private String logShipOrderNo;
    private String finalShipCon;   //최종출고유무 (출고승인 후 입력되는 값!)
}


