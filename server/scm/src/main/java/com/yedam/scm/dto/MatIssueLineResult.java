package com.yedam.scm.dto;

import lombok.Data;

@Data
public class MatIssueLineResult {
    private Long plDetId;     // 생산계획상세 ID
    private String matId;     // 자재코드
    private Double reqQty;    // 요청중량(출고수량)
    private String managerId; // 담당자 ID
}
