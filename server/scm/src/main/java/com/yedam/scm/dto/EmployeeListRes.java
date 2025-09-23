package com.yedam.scm.dto;

import java.util.List;

import com.yedam.scm.vo.EmployeeSimpleVO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmployeeListRes {
  private List<EmployeeSimpleVO> data;
  private PageDTO page;
}
