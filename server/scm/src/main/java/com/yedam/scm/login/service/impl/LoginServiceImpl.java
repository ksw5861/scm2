package com.yedam.scm.login.service.impl;

import org.springframework.stereotype.Service;

import com.yedam.scm.dto.LoginDTO;
import com.yedam.scm.dto.LoginRes;
import com.yedam.scm.login.mapper.LoginMapper;
import com.yedam.scm.login.service.LoginService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final LoginMapper mapper;

    @Override
    public LoginRes loginByEmailAndPassword(LoginDTO login) {

        // 토큰 생성해야함
        return mapper.selectAccountByEmailAndPassword(login);
    }
}
