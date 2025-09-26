package com.yedam.scm.login.service;

import org.springframework.stereotype.Service;

import com.yedam.scm.dto.LoginDTO;
import com.yedam.scm.dto.LoginRes;

public interface LoginService {

    // 이메일과 비밀번호로 로그인
    LoginRes loginByEmailAndPassword(LoginDTO login);
}
