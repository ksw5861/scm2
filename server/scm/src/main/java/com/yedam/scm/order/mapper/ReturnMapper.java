package com.yedam.scm.order.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.yedam.scm.vo.ReturnVO;
import com.yedam.scm.vo.ReturnDetailVO;

@Mapper
public interface ReturnMapper {

    int insertReturnOrder(ReturnVO returnVO);               // 반품 마스터 등록
    int insertReturnDetail(ReturnDetailVO returnDetailVO);  // 반품 상세 등록

    List<ReturnVO> getReturnList(
        @Param("startDate") String startDate,
        @Param("endDate") String endDate,
        @Param("returnStatus") String returnStatus,
        @Param("prodName") String prodName,
        @Param("returnId") String returnId
    ); // 반품 목록 조회

    ReturnVO getReturnDetail(String returnId); // 반품 마스터 단건 조회
    List<ReturnDetailVO> getReturnDetailList(String returnId); // 반품 상세 목록 조회
}
