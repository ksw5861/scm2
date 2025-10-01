package com.yedam.scm.dto;

import java.util.List;

import lombok.Data;

@Data
public class VendorSearchDTO {

  // 거래처 유형
  private List<Integer> type;

  // 거래처 사용 여부
  private List<Character> isActive;

  // 거래처명
  private String companyName;

  // 담당자 이름 or 대표자 이름
  private String name;

  // 담당자 연락처 or 대표자 연락처
  private String phoneNumber;

  // 사업자등록번호
  private String businessRegistration;

}
