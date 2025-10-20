package com.yedam.scm.dto;

import lombok.Data;

@Data
public class AdjStockDTO {
    
    private Long lotId;          // LOT ID
    private Double weight;       // 조정 중량
    private String inOut;        // 증감 구분 (IN / OUT)
    private String reason;       // 조정 사유
    private String name;         // 담당자 이름
    
}
