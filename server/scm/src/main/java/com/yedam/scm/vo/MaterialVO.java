package com.yedam.scm.vo;

import lombok.Data;

@Data
public class MaterialVO {
    private String matId;           // 자재ID
    private String matName;         // 자재명
    private String matType;         // 자재유형
    private String matStoreCond;    // 보관조건
    private Long matUnitConv;       // 단위환산
    private Long matUnitPrice;      // 단가
    private Long leadTime;          // 리드타임
    private Long safeStock;         // 안전재고
    private String status;          // 상태
    private String spec;            // 규격
    private String unit;            // 단위
}