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

    private Long plDetId;      // PK seq부여
    private String prodId;     // 제품 ID 
    private Long proQty;    // 수량 
    private Date proDate;      // 생산일자
    private Long plId;         // 계획 ID (FK)
    private String matStatus;  // 자재 상태
    private String prodNo;     // 제품 LOT번호[고유번호함수 활용]
    private String mprStatus;  // MPR 상태

    ProductVO productVO;
}
