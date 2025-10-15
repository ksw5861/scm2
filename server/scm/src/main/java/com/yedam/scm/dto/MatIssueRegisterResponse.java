package com.yedam.scm.dto;

import lombok.Data;
import java.util.List;

@Data
public class MatIssueRegisterResponse {
    private Long plDetId;
    private String result; // "OK" or "ERROR"
    private List<MatIssueLineResult> lines;
}
