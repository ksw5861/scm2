package com.yedam.scm.master.service;

import java.util.List;

import com.yedam.scm.vo.VendorVO;

public interface VendorService {
    // 거래처 목록 조회
    List<VendorVO> getVendorList(String vendorId, String companyName, String isActive);

    // 거래처 상세 조회
    VendorVO getVendorDetail(String vendorId);

    // 거래처 등록
    int insertVendor(VendorVO vendorVO);

    // 거래처 수정
    int updateVendor(VendorVO vendorVO);

    // 거래처 삭제
    int deleteVendor(String vendorId);
}
