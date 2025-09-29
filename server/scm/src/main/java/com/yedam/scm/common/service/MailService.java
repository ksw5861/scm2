package com.yedam.scm.common.service;

import java.util.List;

import com.yedam.scm.dto.EmailDTO;

public interface MailService {

    // 비동기 이메일 전송
    void sendMailAsync(String to, String subject, String htmlContent, String from);


    // 최근 이메일 목록 조회
    List<EmailDTO> fetchRecentEmails() throws Exception;

}
