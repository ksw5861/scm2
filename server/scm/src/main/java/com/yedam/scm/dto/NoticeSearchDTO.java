package com.yedam.scm.dto;

import lombok.Data;

@Data
public class NoticeSearchDTO {

  // 제목 검색 조건
  private String title;

  // 정렬 조건
  private String sortField;
  private String sortOrder;

}
