package com.yedam.scm.order.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.yedam.scm.vo.ReturnVO;

@Mapper
public interface ReturnMapper {

    // 1. 반품 등록
    int insertReturn(ReturnVO returnVO);

    // 2. 반품 목록 조회
    List<ReturnVO> getReturnList(
        @Param("startDate") String startDate,
        @Param("endDate") String endDate,
        @Param("returnStatus") String returnStatus,
        @Param("prodName") String prodName,
        @Param("returnNo") String returnNo
    );

    // 3. 특정 반품 단건 조회
    ReturnVO getReturnDetail(String returnId);
}
