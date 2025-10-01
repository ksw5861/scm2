package com.yedam.scm.master.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.dto.VendorSearchDTO;
import com.yedam.scm.vo.VendorVO;

@Mapper
public interface VendorMapper {

    // 거래처 목록 조회
    List<VendorVO> getVendorList(
        @Param("condition") VendorSearchDTO condition,
        @Param("paging") PageDTO paging
    );

    long getVendorListTotalCount(@Param("condition") VendorSearchDTO condition);

    // 거래처 상세 조회
    VendorVO getVendorDetail(@Param("vendorId") String vendorId);

    // 거래처 등록
    int insertVendor(VendorVO vendorVO);

    // 거래처 수정
    int updateVendor(VendorVO vendorVO);

    // 거래처 삭제
    int deleteVendor(@Param("vendorId") String vendorId);
}
