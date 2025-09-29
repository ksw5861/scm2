package com.yedam.scm.master.service;

import java.util.List;

import com.yedam.scm.vo.MaterialVendorVO;

public interface MaterialVendorService {
    // 거래처 목록 조회 (자재별)
    List<MaterialVendorVO> getMaterialVendorList(String matId);

    // 거래처 상세 조회
    MaterialVendorVO getMaterialVendorDetail(String matVendorId);

    // 거래처 등록
    int insertMaterialVendor(MaterialVendorVO materialVendorVO);

    // 거래처 수정
    int updateMaterialVendor(MaterialVendorVO materialVendorVO);

    // 거래처 삭제
    int deleteMaterialVendor(String matVendorId);
}
