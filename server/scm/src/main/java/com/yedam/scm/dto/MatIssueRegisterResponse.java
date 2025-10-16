package com.yedam.scm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatIssueRegisterResponse {
    private boolean success;
    private String message;
}
