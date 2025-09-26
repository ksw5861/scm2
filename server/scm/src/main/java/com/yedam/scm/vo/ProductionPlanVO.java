/*
 * 2025.09.22
 * 생산계획VO [master] + 생산계획 세부VO [detail]
 *  
 */

package com.yedam.scm.vo;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class ProductionPlanVO {

    private Long plId;   //pk seq부여
    private Date startDate; // 시작일
    private Date endDate;   // 종료일
    private String planNo;  // 계획번호 -> 함수
    private String empName;  // 사원명 ->로그인세션정보 가져옴
    private Date reDate;    // 등록일 -> sysdate
    private String planType;
    private String memo;

    List<PrdPlanDetailVO> prdPlanDetailList;

}
