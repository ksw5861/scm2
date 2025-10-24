package com.yedam.scm.vo;

import java.util.Date;
import java.util.List;


import lombok.Data;

@Data
public class BomVO {
    private String bomId;
    private String bomVersion;
    private Date effectiveDate;
    private Date expireDate;
    private String prodId;
    private Date createdAt;
    private Double lossRate;
    private Date lastUpdateDate;

    // 조인용
    private ProductVO product;
    private List<BomDetailVO> details;
}


