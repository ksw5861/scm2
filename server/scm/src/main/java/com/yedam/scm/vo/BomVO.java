package com.yedam.scm.vo;

import java.util.Date;
import lombok.Data;

@Data
public class BomVO {
    private String bomId;
    private String bomVersion;
    private Date effectiveDate;
    private Date expireDate;
    private String prodId;
    private String matId;
    private Date createdAt;
    private Integer qty; // 수량

    private Double lossRate;

    private MaterialVO material; // 자재 정보 포함
    private ProductVO product;
    private BomDetailVO bomDetail;
}
