package com.yedam.scm.order.mapper;

import java.util.List;
import java.util.Map;

import com.yedam.scm.vo.SalesMarginVO;
import com.yedam.scm.vo.SalesMasterVO;
import com.yedam.scm.vo.SalesDetailVO;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SalesMarginMapper {

    // 등록
    int deleteAll();
    int insert(SalesMarginVO vo);
   
   
    // 전체 목록 조회
    List<SalesMarginVO> selectSalesMarginList();

    // 단건 조회 (제품코드 기준)
    SalesMarginVO selectSalesMarginById(String saleProdId);



    // 삭제
    int deleteSalesMargin(String saleProdId);

    // ✅ 매출등록 관련
    int insertSaleMaster(SalesMasterVO vo);
    int insertSaleDetail(SalesDetailVO vo);

    // 매출이력조회
    List<SalesMasterVO> getSalesHistory(String vendorId);
}