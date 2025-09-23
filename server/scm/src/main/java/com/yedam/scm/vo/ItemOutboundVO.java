package com.yedam.scm.vo;

import java.time.LocalDate;
import lombok.Data;


//김동원 출고
@Data
public class ItemOutboundVO {

    private String outboundId;    // 출고 ID
    private String odetailId;     // 출고 상세 ID
    private String prdLot;        // 제품 LOT 번호
    private String vendorId;      // 거래처 ID
    private String prodId;        // 제품 ID
    private int outQty;           // 출고 수량
    private LocalDate outDate;    // 출고 일자
    private String employeeId;    // 담당자 ID

}
