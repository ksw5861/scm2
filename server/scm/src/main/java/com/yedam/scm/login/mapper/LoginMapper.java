package com.yedam.scm.login.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.yedam.scm.dto.LoginDTO;
import com.yedam.scm.dto.LoginRes;

@Mapper
public interface LoginMapper {

    // 이메일과 비밀번호로 로그인
    LoginRes selectAccountByEmailAndPassword(LoginDTO login);

}
