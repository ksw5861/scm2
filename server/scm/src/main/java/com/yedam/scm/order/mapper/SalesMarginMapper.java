package com.yedam.scm.order.mapper;

import java.util.List;
import com.yedam.scm.vo.SalesMarginVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SalesMarginMapper {
   
    // 단건 MERGE
    int mergeSalesMargin(SalesMarginVO vo);

    // 다건 MERGE
    int mergeSalesMarginList(List<SalesMarginVO> list);
   
   
    // 전체 목록 조회
    List<SalesMarginVO> selectSalesMarginList();

    // 단건 조회 (제품코드 기준)
    SalesMarginVO selectSalesMarginById(String saleProdId);



    // 삭제
    int deleteSalesMargin(String saleProdId);
}