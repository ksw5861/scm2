package com.yedam.scm.login.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.yedam.scm.dto.RecaptchaRes;
import com.yedam.scm.login.service.RecaptchaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecaptchaServiceImpl implements RecaptchaService {

    @Value("${recaptcha.secret}")
    private String secretKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean verifyToken(String token) {
        String url = "https://www.google.com/recaptcha/api/siteverify" +
                "?secret=" + secretKey +
                "&response=" + token;

        RecaptchaRes response = restTemplate.postForObject(url, null, RecaptchaRes.class);

        if (response == null || !response.isSuccess()) {
            return false;
        }

        return response.getScore() >= 0.5 && "login".equals(response.getAction());
    }
}