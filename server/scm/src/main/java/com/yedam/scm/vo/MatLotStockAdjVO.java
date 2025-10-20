package com.yedam.scm.vo;

import java.util.Date;

import lombok.Data;

@Data
public class MatLotStockAdjVO {

    private Long adjStockId;     // 조정이력 ID     // 등록시 OUT: 프로시저에서 받아옴
    private Date reDate;         // 조정일자 (RE_DATE)
    private Double afterWeight;  // 조정 후 중량
    private String name;         // 담당자 이름
    private Double beforeWeight; // 조정 전 중량
    private String reason;       // 조정 사유
    private Long lotId;          // LOT ID
    private String inOut;        // 증감 구분 (IN / OUT)
    private Double weight;       // 조정 중량

}
