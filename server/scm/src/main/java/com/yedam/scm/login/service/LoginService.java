package com.yedam.scm.login.service;

import com.yedam.scm.dto.LoginDTO;
import com.yedam.scm.dto.LoginRes;

public interface LoginService {

    // 이메일과 비밀번호로 로그인
    LoginRes loginByEmailAndPassword(LoginDTO login);

    // 비밀번호 변경
    boolean modifyAccountPassword(String accountId, String newPassword);
}
