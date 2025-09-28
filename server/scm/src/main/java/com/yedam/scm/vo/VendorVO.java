package com.yedam.scm.vo;

import java.util.Date;

import lombok.Data;

@Data
public class VendorVO {
    private String vendorId; // 거래처 번호 (PK)
    private String businessRegistration; // 사업자등록번호
    private String companyName; // 업체명
    private String ceoName; // 대표자명
    private String phoneNumber; // 연락처
    private String address; // 주소
    private String isActive; // 사용여부 (Y/N)
    private Date createdAt; // 생성일자
    private Date updatedAt; // 수정일자
}