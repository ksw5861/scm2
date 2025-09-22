
/*
 * 2025.09.22
 * 생산계획 세부VO [detail]
 *  
 */



package com.yedam.scm.vo;

import java.util.Date;
import lombok.Data;

@Data
public class PrdPlanDetailVO {

    private Long plId;
    private Date startDate;
    private Date endDate;
    private String planNo;
    private String empName;
    private Date reDate;


}
