package com.yedam.scm.vo;

import lombok.Data;

@Data
public class BomDetailVO {
    private Long bomDeId;
    private String bomId;
    private String matId;
    private Double mixingRate;
    private String baseUnit;

    private MaterialVO material; // 자재 정보 포함

}
