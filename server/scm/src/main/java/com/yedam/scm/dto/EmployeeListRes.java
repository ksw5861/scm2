package com.yedam.scm.dto;

import java.util.List;

import com.yedam.scm.vo.EmployeeVO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmployeeListRes {
  private List<EmployeeVO> data;
  private PageDTO page;
}
