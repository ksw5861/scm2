
/*
 * 2025.09.22
 * 생산계획 세부VO [detail]
 *  
 */



package com.yedam.scm.vo;

import java.util.Date;
import lombok.Data;

//생산세부계획

@Data
public class PrdPlanDetailVO {

    private Long plDetId;      // PK
    private String prodId;     // 제품 ID
    private Integer proQty;    // 수량
    private Date proDate;      // 생산일자
    private Long plId;         // 계획 ID (FK)
    private String matStatus;  // 자재 상태
    private String prodNo;     // 제품번호
    private String mprStatus;  // MPR 상태

}
