package com.yedam.scm.vo;


import lombok.Data;

@Data
public class ProductVO {
    private String prodId;
    private String prodName;
    private String prodStoreCond;
    private int safeStock;
    private String status;
    private String spec;
    private String unit;
    private int exp;
    private int prodUnitPrice; // NULL 들어와도 문제 없음
}
