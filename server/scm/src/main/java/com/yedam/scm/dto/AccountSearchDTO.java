package com.yedam.scm.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountSearchDTO {

  // 사용여부
  private List<Character> isActive;

  // 계정 유형
  private List<Character> type;

  // 사원번호 또는 업체 번호
  private String code;
  
  // 업체명 또는 사원명
  private String name;

  // 이메일
  private String email;

  // 전화번호
  private String phone;

}
