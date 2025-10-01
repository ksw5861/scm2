package com.yedam.scm.vo;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yedam.scm.dto.PageDTO;

import lombok.Data;

@Data
public class VendorVO {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String vendorId;               // 거래처 번호 (PK)

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String businessRegistration;    // 사업자등록번호

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String companyName;            // 업체명

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String ceoName;                // 대표자명

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String phoneNumber;            // 연락처

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String address;                // 주소

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String isActive;               // 사용여부 (Y/N)
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date createdAt;                // 생성일자

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date updatedAt;                // 수정일자

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String type;                   // 거래처 유형

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String ownerName;              // 담당자명

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String ownerEmail;             // 담당자 이메일

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String ownerTel;               // 담당자 연락처

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PageDTO> paging;           // 페이지네이션
}
