package com.yedam.scm.vo;


import java.util.Date;

import lombok.Data;

@Data
public class PurStatusLogVO {

    private Long purStatusId;    // PUR_STATUS_ID
    private Date reDate;         // RE_DATE
    private String name;         // NAME
    private String purMatStatus; // PUR_MAT_STATUS
    private Long purId;          // PUR_ID
    private Long supOutQty;      //공급처 출고수량 (for 부분출고)
    private String vendorId; 
}
