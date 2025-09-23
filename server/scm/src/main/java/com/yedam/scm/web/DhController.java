package com.yedam.scm.web;

import org.springframework.web.bind.annotation.RestController;

import com.yedam.scm.dto.EmployeeListRes;
import com.yedam.scm.dto.EmployeeSearchDTO;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.master.service.EmployeeService;
import com.yedam.scm.vo.EmployeeSimpleVO;
import com.yedam.scm.vo.EmployeeVO;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;


@RestController
@RequiredArgsConstructor
public class DhController {

  private final EmployeeService employeeSvc;

  @GetMapping("/employeelist")
  public EmployeeListRes getEmployeeListByCondition(
    @ModelAttribute EmployeeSearchDTO condition, 
    @ModelAttribute PageDTO paging
  ) {

    return employeeSvc.getEmployeeSimpleListByCondition(condition, paging);

  }
  

}