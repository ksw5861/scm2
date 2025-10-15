package com.yedam.scm.order.mapper;

import java.util.List;
import com.yedam.scm.vo.SalesMarginVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SalesMarginMapper {
    // 전체 목록 조회
    List<SalesMarginVO> selectSalesMarginList();

    // 단건 조회 (제품코드 기준)
    SalesMarginVO selectSalesMarginById(String saleProdId);

    // 신규 등록
    int insertSalesMargin(SalesMarginVO vo);

    // 수정 (마진율, POS 노출, 순서 등)
    int updateSalesMargin(SalesMarginVO vo);

    // 삭제
    int deleteSalesMargin(String saleProdId);
}