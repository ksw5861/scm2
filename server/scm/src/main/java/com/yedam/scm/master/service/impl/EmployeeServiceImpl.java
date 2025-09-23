package com.yedam.scm.master.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yedam.scm.dto.EmployeeListRes;
import com.yedam.scm.dto.EmployeeSearchDTO;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.master.mapper.EmployeeMapper;
import com.yedam.scm.master.service.EmployeeService;
import com.yedam.scm.vo.EmployeeSimpleVO;
import com.yedam.scm.vo.EmployeeVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

  private final EmployeeMapper mapper;

  @Override
  public EmployeeListRes getEmployeeSimpleListByCondition(EmployeeSearchDTO condition, PageDTO paging) {

    paging.updatePageInfo(mapper.selectEmployeeCountByCondition(condition));
    List<EmployeeSimpleVO> data = mapper.selectEmployeeSimpleListByCondition(condition, paging);

    return new EmployeeListRes(data, paging);

  }

  @Override
  public EmployeeVO getEmployeeById(String empId) {

    return mapper.selectEmployeeById(empId);
    
  }

  @Override
  public boolean removeEmployeeById(String empId) {

    return mapper.deleteEmployeeById(empId) > 0;
  }

}
