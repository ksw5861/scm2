package com.yedam.scm.order.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.yedam.scm.vo.ReturnVO;

@Mapper
public interface ReturnMapper {

    // 반품 등록
    int insertReturn(ReturnVO returnVO);

    // 반품 목록 조회
    List<ReturnVO> getReturnList(String startDate, String endDate, String status);

    // 특정 반품 단건 조회
    ReturnVO getReturnDetail(String returnId);
}