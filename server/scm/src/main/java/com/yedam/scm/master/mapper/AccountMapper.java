package com.yedam.scm.master.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.yedam.scm.dto.AccountSearchDTO;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.vo.AccountVO;

@Mapper
public interface AccountMapper {

  // 조건에 맞는 총 계정 수 조회
  long selectAccountCountByCondition(
    @Param("condition") AccountSearchDTO condition
  );

  // 조건에 맞는 계정 목록 조회
  List<AccountVO> selectAccountListByCondition(
    @Param("condition") AccountSearchDTO condition,
    @Param("paging") PageDTO paging
  );

  // 활성화 여부 수정
  int updateUseYnByAccountId(String accountId, Character isActive);

  // 바말번호 재설정
  int setPassword(String accountId, String password);

  // 이메일 정보 불러오기
  String getEmail(String accountId);

}
