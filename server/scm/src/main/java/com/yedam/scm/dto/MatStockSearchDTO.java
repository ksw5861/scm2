package com.yedam.scm.dto;

import lombok.Data;

@Data
public class MatStockSearchDTO {

  //검색조건
  private String materialId;
  private String materialName;
  private String lotNo;
  private String lotStatus;
}
