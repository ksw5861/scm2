package com.yedam.scm.login.service.impl;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.yedam.scm.dto.LoginDTO;
import com.yedam.scm.dto.LoginRes;
import com.yedam.scm.login.mapper.LoginMapper;
import com.yedam.scm.login.service.LoginService;
import com.yedam.scm.login.service.RecaptchaService;
import com.yedam.scm.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final LoginMapper mapper;
    private final JwtUtil jwtUtil;
    private final RecaptchaService recaptchaService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginRes loginByEmailAndPassword(LoginDTO login) {
        LoginRes res = new LoginRes();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        if (!recaptchaService.verifyToken(login.getRecaptcha())) {
            res.setVerifyRecaptcha(false);
            return res;
        }
        res.setVerifyRecaptcha(true);

        LoginRes dbUser = mapper.selectAccountByEmailAndPassword(login);

        if (dbUser == null) {
            return null;
        }

        boolean isMatch = encoder.matches(login.getPassword(), dbUser.getPasswordHash());

        if (isMatch) {
            dbUser.setAccessToken(jwtUtil.generateToken(dbUser));
            dbUser.setVerifyRecaptcha(true);
            return dbUser;
        } else {
            return null;
        }
    }

    @Override
    public boolean modifyAccountPassword(String accountId, String password) {

        return mapper.updateAccountPassword(accountId ,passwordEncoder.encode(password)) > 0;

    }
}
