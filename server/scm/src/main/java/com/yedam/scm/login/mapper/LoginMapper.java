package com.yedam.scm.login.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.yedam.scm.dto.LoginDTO;
import com.yedam.scm.dto.LoginRes;

@Mapper
public interface LoginMapper {

    // 이메일과 비밀번호로 로그인
    LoginRes selectAccountByEmailAndPassword(LoginDTO login);

    LoginRes selectAdminAccountByEmailAndPassword(LoginDTO login);

    // 계정 번호 + 암호화된 비밀번호
    int updateAccountPassword(
        @Param("accountId") String accountId,
        @Param("password") String password
    );
    
    // 계정 번호로 계정 조회
    LoginRes selectAccountByAccountId(String accountId);

}
