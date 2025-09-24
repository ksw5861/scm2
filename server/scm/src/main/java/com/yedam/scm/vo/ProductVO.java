package com.yedam.scm.vo;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class ProductVO {
    private String prodId;
    private String prodName;
    private Date prodExpireDate;
    private int safeStock;
    private String status;
    private String spec;
    private String unit;
    private BigDecimal prodPrice; // NULL 들어와도 문제 없음
}
