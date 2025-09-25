package com.yedam.scm.vo;

import java.util.Date;
import lombok.Data;

@Data
public class BomVO {
    private String bomId;
    private String bomVer;
    private Date effectiveDate;
    private Date expireDate;
    private String prodId;
    private String matId;
    private Date createdAt;
    private Integer qty;

    private MaterialVO material; // 자재 정보 포함
}
