package com.yedam.scm.master.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.yedam.scm.vo.MaterialVendorVO;

@Mapper
public interface MaterialVendor {
    // 거래처 목록 조회 (자재별)
    List<MaterialVendorVO> getMaterialVendorList(@Param("matId") String matId);

    // 거래처 상세 조회
    MaterialVendorVO getMaterialVendorDetail(@Param("matVendorId") String matVendorId);

    // 거래처 등록
    int insertMaterialVendor(MaterialVendorVO materialVendorVO);

    // 거래처 수정
    int updateMaterialVendor(MaterialVendorVO materialVendorVO);

    // 거래처 삭제
    int deleteMaterialVendor(@Param("matVendorId") String matVendorId);
}
