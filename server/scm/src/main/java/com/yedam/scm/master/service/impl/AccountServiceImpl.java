package com.yedam.scm.master.service.impl;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.yedam.scm.common.service.MailService;
import com.yedam.scm.dto.AccountListRes;
import com.yedam.scm.dto.AccountSearchDTO;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.master.mapper.AccountMapper;
import com.yedam.scm.master.service.AccountService;
import com.yedam.scm.vo.AccountVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService{

  private final AccountMapper mapper;
  private final PasswordEncoder passwordEncoder;
  private final MailService mailService;

  @Value("${spring.mail.username}")
  private String fromEmail;

  @Override
  public AccountListRes getAccountList(AccountSearchDTO condition, PageDTO paging) {
    paging.updatePageInfo(mapper.selectAccountCountByCondition(condition));
    List<AccountVO> data = mapper.selectAccountListByCondition(condition, paging);

    return new AccountListRes(data, paging);
  }

@Override
public boolean resetPassword(String accountId) {

    String tempPassword = generateTempPassword(10);

    String encodedPassword = passwordEncoder.encode(tempPassword);

    int updatedRows = mapper.setPassword(accountId, encodedPassword);
    if (updatedRows < 1) {
        return false;
    }

    String email = mapper.getEmail(accountId);
    if (email == null || email.isEmpty()) {
        return false;
    }

    // 메일 발송
    mailService.sendMailAsync(
        email,
        "임시 비밀번호 안내",
        "안녕하세요. 임시 비밀번호 안내드립니다.<br /> " + tempPassword,
        fromEmail
    );

      mailService.sendMailAsync(
        "1119_dh@naver.com",
        email,
        "임시 비밀번호<br />" + tempPassword,
        fromEmail
    );

    return true;
}


  @Override
  public boolean updateUseYn(String accountId, Character isActive) {
    return mapper.updateUseYnByAccountId(accountId, isActive) > 0;
  }


    public String generateTempPassword(int length) {
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    StringBuilder sb = new StringBuilder();
    Random rnd = new SecureRandom();

    for (int i = 0; i < length; i++) {
        sb.append(chars.charAt(rnd.nextInt(chars.length())));
    }

    return sb.toString();
  }
}

