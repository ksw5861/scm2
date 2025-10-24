package com.yedam.scm.vo;

import lombok.Data;
import java.util.Date;

@Data
public class SalesMarginVO {
    private String saleProdId;      // 제품코드 (PK)
    private String saleProdName;    // 제품명
    private Integer saleProdPrice;  // 판매가(손님판매가)
    private Integer prodUnitPrice;  // 제품단가=본사구매가
    private Double saleMargin;      // 마진율(%)
    private Integer sortNo;         // POS 정렬순서
    private String posShowYn;       // POS 노출여부 (Y/N)
    private Date createDt;          // 등록일
    private Date updateDt;          // 수정일
    private String vendorId;      // 판매처 코드
}