package com.yedam.scm.dto;

import lombok.Data;

@Data
public class prodPlanForAccoDTO {
   
    private String startDate;  //월 
    private String endDate;    //주 (공통코드로 변경됨)
    //private String prodName;
    private String status;
}
