package com.yedam.scm.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
public class LoginRes {

    private String accessToken;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String accountId;

    @JsonIgnore
    private String passwordHash;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String code;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String role;
}
