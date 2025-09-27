package com.yedam.scm.vo;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
public class EmployeeVO {

  private String employeeId;

  private String name;

  private String phone;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String email;

  @JsonInclude(JsonInclude.Include.NON_NULL)

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private Date hireDate;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private Date resignDate;
  
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String status;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private char isActive;

  @JsonInclude(JsonInclude.Include.NON_NULL)
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
