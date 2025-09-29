package com.yedam.scm.master.service;

import java.io.IOException;

import org.springframework.http.ResponseEntity;

import com.yedam.scm.dto.EmployeeListRes;
import com.yedam.scm.dto.EmployeeSearchDTO;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.vo.EmployeeVO;

import org.springframework.core.io.Resource;

public interface EmployeeService {

  // 사원 이미지 조회
  ResponseEntity<Resource> getEmployeeImage(String employeeId) throws IOException;

  // 검색 조건 필터링된 사원번호 + 사원명 조회
  EmployeeListRes getEmployeeSimpleListByCondition(EmployeeSearchDTO condition, PageDTO paging);

  // 사원번호로 단건 조회
  EmployeeVO getEmployeeById(String empId);

  // 사원 등록
  boolean addEmployee(EmployeeVO emp) throws IOException;

  // 사원 정보 수정
  boolean modifyEmployeeById(EmployeeVO emp) throws IOException;

  // 사원번호로 단건 삭제
  boolean removeEmployeeById(String empId) throws IOException;
  
}
