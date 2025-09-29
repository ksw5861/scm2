package com.yedam.scm.vo;

import java.util.Date;

import lombok.Data;

@Data
public class VendorVO {
    
    private String vendorId;
    private String businessRegistration;
    private String companyName;
    private String ceoName;
    private String phoneNumber;
    private String address;
    private char isActive;
    private Date createdAt;
    private Date updatedAt;

}
