package com.yedam.scm.master.service;

import java.util.Map;

import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.dto.VendorSearchDTO;
import com.yedam.scm.vo.VendorVO;

public interface VendorService {
    
    // 거래처 목록 조회
    Map<String, Object> getVendorList(VendorSearchDTO condition, PageDTO paging);

    // 거래처 상세 조회
    VendorVO getVendorDetail(String vendorId);

    // 거래처 등록
    boolean insertVendor(VendorVO vendor);

    // 거래처 수정
    boolean updateVendor(VendorVO vendor);

    // 거래처 삭제
    boolean deleteVendor(String vendorId);
}
