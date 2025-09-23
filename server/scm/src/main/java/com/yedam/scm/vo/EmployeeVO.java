package com.yedam.scm.vo;

import java.util.Date;

import lombok.Data;

@Data
public class EmployeeVO {

  private String employeeId;
  private String name;
  private String email;
  private String phone;
  private Date hireDate;
  private Date resignDate;
  private String status;
  private char isActive;
  
}
