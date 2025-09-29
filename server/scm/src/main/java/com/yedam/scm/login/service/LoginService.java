package com.yedam.scm.login.service;

import com.yedam.scm.dto.AuthRes;
import com.yedam.scm.dto.LoginDTO;
import com.yedam.scm.dto.LoginRes;

public interface LoginService {

    // 이메일과 비밀번호로 로그인
    LoginRes loginByEmailAndPassword(LoginDTO login);

    // 비밀번호 변경
    boolean modifyAccountPassword(String accountId, String newPassword);

    // QR 생성
    String generateQRCodeImage(String text, int width, int height) throws Exception;

    // 1차 로그인
    AuthRes processTempLogin(LoginDTO login, String serverEmail) throws Exception;

    // AccountId로 계정조회
    LoginRes getAccountByAccountId(String accountId);
}
