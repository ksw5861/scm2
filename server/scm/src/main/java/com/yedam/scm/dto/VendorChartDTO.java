package com.yedam.scm.dto;

import lombok.Data;

@Data
public class VendorChartDTO {
    private String matName;  // 자재명
    private Long qty;        // 출고 수량 합계
}