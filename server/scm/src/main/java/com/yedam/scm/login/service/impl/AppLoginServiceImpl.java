package com.yedam.scm.login.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.yedam.scm.dto.LoginDTO;
import com.yedam.scm.dto.LoginRes;
import com.yedam.scm.login.mapper.LoginMapper;
import com.yedam.scm.login.service.AppLoginService;
import com.yedam.scm.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppLoginServiceImpl implements AppLoginService {
    private final LoginMapper mapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginRes loginForApp(LoginDTO login) {
        LoginRes dbUser = mapper.selectAccountByEmailAndPassword(login);
        if (dbUser == null) return null;

        boolean isMatch = passwordEncoder.matches(login.getPassword(), dbUser.getPasswordHash());
        if (!isMatch) return null;

        dbUser.setAccessToken(jwtUtil.generateToken(dbUser));
        return dbUser;
    }
}
