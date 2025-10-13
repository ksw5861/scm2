package com.yedam.scm.master.service;

import com.yedam.scm.dto.AccountListRes;
import com.yedam.scm.dto.AccountSearchDTO;
import com.yedam.scm.dto.PageDTO;

public interface AccountService {
  
  // 계정 조회
  AccountListRes getAccountList(
    AccountSearchDTO condition,
    PageDTO paging
  );

  // 바밀번호 재설정 이메일 발송
  boolean resetPassword(String accountId);

  // 사용 여부 업데이트
  boolean updateUseYn(String accountId, Character isActive);

}
