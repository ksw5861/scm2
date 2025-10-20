package com.yedam.scm.order.service;

import com.yedam.scm.vo.SalesMarginVO;
import com.yedam.scm.vo.SalesMasterVO;
import com.yedam.scm.vo.SalesOrderVO;

import java.util.List;
import java.util.Map;


public interface SalesMarginService {

    // int save(SalesMarginVO vo);

    // 포스설정에서 기존데이터삭제후 모두 새로저장
    int saveAll(List<SalesMarginVO> list);

    // 전체 목록 조회
    List<SalesMarginVO> getList();

    SalesMarginVO getById(String saleProdId);

    

    int delete(String saleProdId);

    //포스기 화면에서 결제등록
    void registerSale(SalesMasterVO vo);

    // 매출이력조회
    List<SalesMasterVO> getSalesHistory(String vendorId);

    // 매출 요약 (오늘 vs 어제
    Map<String, Object> getDailySummary(String vendorId);

    // 월별 매출 요약
    Map<String, Object> getMonthlySummary(String vendorId, int year, int month);

    // 대시보드용 매출 추이
    List<Map<String, Object>> getSalesTrend(String vendorId, String range);
    Map<String, Object> getSalesCompare(String vendorId, String range);
    List<Map<String, Object>> getCoffeeRank(String vendorId, String range);

    // 매출 성장률
    Map<String, Object> getSalesGrowth(String vendorId);

    // 결제수단별 매출 비중
    List<Map<String, Object>> getPayMethod(String vendorId, String range);

   // 대시보드에 1일부터 말일까지 출고완료,배송완료에 결제대기인건, 15일자 기준 미수금 계산
    SalesOrderVO getNextDueAmount(String vendorId);
    // 대시보드에 여신한도 잔액
    SalesOrderVO getSalesFinanceSummary(String vendorId);
}