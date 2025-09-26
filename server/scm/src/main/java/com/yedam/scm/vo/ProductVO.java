package com.yedam.scm.vo;

import java.math.BigDecimal;
import java.sql.Date;

import java.math.BigDecimal;
import java.sql.Date;

import lombok.Data;

@Data
public class ProductVO {

    private String prodId;            // 제품 코드
    private String prodName;          // 제품명
    private Date prodExpireDate;      // 유통기한
    private int exp;                  // exp
    private String prodStoreCond;     // 보관조건
    private int safeStock;            // 안전재고
    private String status;            // 제품 상태 (활성/비활성 등)
    private String spec;              // 규격
    private String unit;              // 단위

    // ✅ PROD_UNIT_PRICE 컬럼 사용
    // null 값 허용을 위해 BigDecimal 사용
    private BigDecimal prodUnitPrice;

}
