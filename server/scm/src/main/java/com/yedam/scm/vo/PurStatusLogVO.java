package com.yedam.scm.vo;

import java.sql.Date;

import lombok.Data;

@Data
public class PurStatusLogVO {

    private Long purStatusId;    // PUR_STATUS_ID
    private Date reDate;         // RE_DATE
    private String name;         // NAME
    private String purMatStatus; // PUR_MAT_STATUS
    private Long purId;          // PUR_ID
}
