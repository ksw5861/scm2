package com.yedam.scm.order.mapper;

import java.util.List;
import java.util.Map;

import com.yedam.scm.vo.SalesMarginVO;
import com.yedam.scm.vo.SalesMasterVO;
import com.yedam.scm.vo.SalesDetailVO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    // 📌 매출 요약 (오늘 vs 어제
    Map<String, Object> getDailySales(@Param("vendorId") String vendorId, @Param("dayOffset") int dayOffset);


    // 월별 매출 요약
    // 이번 달 총 매출
    Integer getMonthlyTotal(@Param("vendorId") String vendorId,
                            @Param("year") int year,
                            @Param("month") int month);

    // 일자별 매출
    List<Map<String, Object>> getMonthlyDailySales(
        @Param("vendorId") String vendorId,
        @Param("year") int year,
        @Param("month") int month
    );

    // 영업일 수 (매출 발생한 일자 수)
    Integer getWorkingDays(@Param("vendorId") String vendorId,
                           @Param("year") int year,
                           @Param("month") int month);


// 최근 7일 일별 매출 추이
List<Map<String, Object>> getSalesTrendDaily(@Param("vendorId") String vendorId);

// 최근 6개월 월별 매출 추이
List<Map<String, Object>> getSalesTrendMonthly(@Param("vendorId") String vendorId);
List<Map<String, Object>> getSalesCompare(@Param("vendorId") String vendorId);
List<Map<String, Object>> getCoffeeRank(@Param("vendorId") String vendorId);


// 매출 성장률
Map<String, Object> selectSalesGrowth(String vendorId);


}