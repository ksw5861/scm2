package com.yedam.scm.dto;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class AccountLedgerSearchDTO {

        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private Date startDate;

        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private Date endDate;

        private String vendorId;

}
