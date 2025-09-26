package com.yedam.scm.vo;

import lombok.Data;

@Data
public class AccountVO {
    private String accountId;
    private String code;
    private String name;
    private String email;
    private String phone;
    private String status;
    private String createdAt;
    private String division;
}
