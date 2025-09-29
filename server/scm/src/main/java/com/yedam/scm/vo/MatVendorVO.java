package com.yedam.scm.vo;

import java.sql.Date;

import lombok.Data;

@Data
public class MatVendorVO {
    
    private String matVendorId;
    private Long contractPrice;
    private String matId;
    private String vendorId;
    private String createdBy;
    private Date createdAt;
    private String status;

    VendorVO vendorVO;
    MaterialVO materialVO;
}
