package com.yedam.scm.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class AccountVO {

    private String accountId;
    private String code;
    private String name;
    private String email;
    private String phone;
    private Character isActive;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Date createdAt;
    
    private String division;

}
