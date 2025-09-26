package com.yedam.scm.vo;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class EmployeeVO {

  private String employeeId;
  private String name;
  private String email;
  private String phone;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private Date hireDate;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private Date resignDate;
  
  private String status;
  private char isActive;
  private char hasPhoto;

  // 이미지 파일 첨부
  @JsonIgnore
  private MultipartFile photo;
  
  // 임시 비밀번호
  @JsonIgnore
  private String tempPassword;

  // 결과 반환
  @JsonIgnore
  private int rowCount;
  
}
