package com.yedam.scm.dto;

import lombok.Data;

@Data
public class LoginRes {

    private String accessToken;
    private String email;
    private String role;
    private String name;
    
}
