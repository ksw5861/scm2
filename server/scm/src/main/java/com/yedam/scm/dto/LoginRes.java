package com.yedam.scm.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
public class LoginRes {

    @JsonIgnore
    private String accessToken;

    @JsonIgnore
    private String passwordHash;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String accountId;

    @JsonIgnore
    private String phone;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String code;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String role;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean verifyRecaptcha;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String tempPassword;
}
