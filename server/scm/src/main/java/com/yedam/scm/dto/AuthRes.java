package com.yedam.scm.dto;

import lombok.Data;

@Data
public class AuthRes {
    private String message;
    private String tempToken;
    private String qrCodeImage;
    private String smsUrl;
}