package com.yedam.scm.login.service.impl;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.yedam.scm.dto.LoginDTO;
import com.yedam.scm.dto.LoginRes;
import com.yedam.scm.login.mapper.LoginMapper;
import com.yedam.scm.login.service.LoginService;
import com.yedam.scm.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final LoginMapper mapper;
    private final JwtUtil jwtUtil;

    @Override
    public LoginRes loginByEmailAndPassword(LoginDTO login) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        LoginRes res = mapper.selectAccountByEmailAndPassword(login);

        boolean isMatch = encoder.matches(login.getPassword(), res.getPasswordHash());

        if (isMatch) {
            res.setAccessToken(jwtUtil.generateToken(res));
            return res;
        } else {
            return null;
        }
    }
}
