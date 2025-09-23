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

    private Long plId;
    private Date startDate;
    private Date endDate;
    private String planNo;
    private String empName;
    private Date reDate;

    List<PrdPlanDetailVO> prdPlanDetailList;

}
