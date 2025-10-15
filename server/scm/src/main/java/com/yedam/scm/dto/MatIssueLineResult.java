package com.yedam.scm.dto;

import lombok.Data;

@Data
public class MatIssueLineResult {
    private String matId;
    private Double requestedQty;
    private String status;   // "OK" or "ERROR"
    private String message;  // 에러 메시지나 설명
}
