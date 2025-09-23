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
    private Integer qty;
}
