package com.yedam.scm.master.service;

import com.yedam.scm.dto.EmployeeListRes;
import com.yedam.scm.dto.EmployeeSearchDTO;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.vo.EmployeeVO;

public interface EmployeeService {

  // 검색 조건 필터링된 사원번호 + 사원명 조회
  EmployeeListRes getEmployeeSimpleListByCondition(EmployeeSearchDTO condition, PageDTO paging);

  // 사원번호로 단건 조회
  EmployeeVO getEmployeeById(String empId);

  // 사원번호로 단건 삭제
  boolean removeEmployeeById(String empId);
  
}
