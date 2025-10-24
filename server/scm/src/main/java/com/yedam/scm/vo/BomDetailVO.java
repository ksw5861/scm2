package com.yedam.scm.vo;

import lombok.Data;

@Data
public class BomDetailVO {
    private String bomDeId;
    private String bomId;
    private String matId;
    private Double mixingRate;
    private Double qty;       // 남기는 걸로 결정
    private String baseUnit;

    private MaterialVO material; // 조인 시 사용
}

