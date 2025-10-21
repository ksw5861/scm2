package com.yedam.scm.dto;

import lombok.Data;

@Data
public class VendorDefectDTO {

    private String venOutNo;       // 출고번호 (i.ven_out_no)
    private String matName;        // 자재명 (m.mat_name)
    private Double logRejQty;      // 불량수량 (l.log_rej_qty)
    private String logMemo;        // 불량사유 (l.log_memo)
    private String vendorOutDate;  // 출고일자 (i.vendor_out_date)//private
    
}
