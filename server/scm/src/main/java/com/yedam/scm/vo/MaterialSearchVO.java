package com.yedam.scm.vo;

import lombok.Data;

@Data
public class MaterialSearchVO {
    private String matId;
    private String matName;
    private String matType;
    private String matStoreCond;

    private String sortField;  // matId, matName 등
    private Integer sortOrder; // 1: ASC, -1: DESC

    private Integer page;      // 현재 페이지
    private Integer size;      // 페이지당 건수
}
