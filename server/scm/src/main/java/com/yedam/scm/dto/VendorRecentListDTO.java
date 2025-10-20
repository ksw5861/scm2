package com.yedam.scm.dto;

import lombok.Data;

@Data
public class VendorRecentListDTO {

    private String type;        // 구분 (주문/출고)
    private String refNo;       // 번호
    private String matId;       // 자재코드
    private String regDate;     // 일자
    private double qty;         // 수량

}
