package com.yedam.scm.dto;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class MatUnloadSearchDTO {
    @DateTimeFormat(pattern = "yyyy-MM-dd")  //프론트 -> 백으로 넘어오는 문자열 변환 [프->백에 넘어오는 시점에 작동]
    private Date startDate;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;
    
    private String vendorName;

    private String status;

}
