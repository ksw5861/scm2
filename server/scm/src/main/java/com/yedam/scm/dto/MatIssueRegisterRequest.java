package com.yedam.scm.dto;

import lombok.Data;
import java.util.List;

@Data
public class MatIssueRegisterRequest {
    private List<MatIssueLineResult> lines;
}
