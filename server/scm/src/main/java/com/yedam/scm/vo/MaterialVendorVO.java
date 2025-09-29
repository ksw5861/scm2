package com.yedam.scm.vo;

import lombok.Data;

@Data
public class MaterialVendorVO {
    private String matVendorId;
    private Double contractPrice;
    private String matId;
    private String vendorId;
    private String createdBy;
    private java.util.Date createdAt;
    private String status;

    private String vendorName;
}
