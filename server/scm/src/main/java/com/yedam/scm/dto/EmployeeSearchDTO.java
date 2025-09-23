package com.yedam.scm.dto;

import java.util.List;

import lombok.Data;

@Data
public class EmployeeSearchDTO {

  // 사원명 검색
  private String empName;

  // 연락처 검색
  private String phone;

  // 사원 번호 검색
  private String empId;

  // 재직 상태 조건
  private List<Integer> status;

  // 사용 여부 조건
  private List<Character> isActive;

  // 입사일 범위 검색
  private String startHireDate;
  private String endHireDate;

  // 정렬 조건
  private String sortField;
  private String sortOrder;

}
