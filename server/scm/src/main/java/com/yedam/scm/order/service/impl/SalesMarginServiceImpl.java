package com.yedam.scm.order.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.scm.order.mapper.SalesMarginMapper;
import com.yedam.scm.order.service.SalesMarginService;
import com.yedam.scm.vo.SalesDetailVO;
import com.yedam.scm.vo.SalesMarginVO;
import com.yedam.scm.vo.SalesMasterVO;

@Service
public class SalesMarginServiceImpl implements SalesMarginService {

    @Autowired
    private SalesMarginMapper mapper;

    @Override
    public List<SalesMarginVO> getList() {
        return mapper.selectSalesMarginList();
    }

    @Override
    public SalesMarginVO getById(String saleProdId) {
        return mapper.selectSalesMarginById(saleProdId);
    }

    @Transactional
    @Override
    public int saveAll(List<SalesMarginVO> list) {
        // 1️⃣ 기존 데이터 전체 삭제
        mapper.deleteAll();

        // 2️⃣ 새 데이터 삽입
        int insertedCount = 0;
        for (SalesMarginVO vo : list) {
            insertedCount += mapper.insert(vo);
        }

        // 3️⃣ 결과 반환
        return insertedCount;
    }

    @Override
    public int delete(String saleProdId) {
        return mapper.deleteSalesMargin(saleProdId);
    }

    // ✅ POS 결제 등록
    @Override
    @Transactional
    public void registerSale(SalesMasterVO vo) {
        mapper.insertSaleMaster(vo); // 판매 마스터 등록

        if (vo.getSalesDetails() != null && !vo.getSalesDetails().isEmpty()) {
            for (SalesDetailVO detail : vo.getSalesDetails()) {
                detail.setSaleId(vo.getSaleId());
                mapper.insertSaleDetail(detail); // 여기서 당시 상품정보 복사됨
            }
        }
    }


    // ✅ 매출이력조회
    @Override
    public List<SalesMasterVO> getSalesHistory(String vendorId) {
        return mapper.getSalesHistory(vendorId);
    }

    // 📌 매출 요약 (오늘 vs 어제
    @Override
    public Map<String, Object> getDailySummary(String vendorId) {
        Map<String, Object> result = new HashMap<>();

        // 오늘
        Map<String, Object> today = mapper.getDailySales(vendorId, 0);
        // 어제
        Map<String, Object> yesterday = mapper.getDailySales(vendorId, 1);

        result.put("today", today.get("TOTAL_SALES"));
        result.put("todayCount", today.get("ORDER_COUNT"));
        result.put("yesterday", yesterday.get("TOTAL_SALES"));
        result.put("yesterdayCount", yesterday.get("ORDER_COUNT"));

        return result;
    }

    // 월별 매출 요약
   @Override
    public Map<String, Object> getMonthlySummary(String vendorId, int year, int month) {
        Map<String, Object> result = new HashMap<>();

        // ✅ 이번 달 총 매출
        Integer thisMonthTotal = mapper.getMonthlyTotal(vendorId, year, month);
        result.put("total", thisMonthTotal == null ? 0 : thisMonthTotal);

        // ✅ 이번 달 영업일 수 (매출이 있는 날짜 수)
        Integer workingDays = mapper.getWorkingDays(vendorId, year, month);
        result.put("workingDays", workingDays == null ? 0 : workingDays);

        // ✅ 일자별 매출 리스트
        List<Map<String, Object>> dailySales = mapper.getMonthlyDailySales(vendorId, year, month);
        result.put("dailySales", dailySales); 

        // ✅ 전월 총 매출
        int prevMonth = (month == 1) ? 12 : month - 1;
        int prevYear = (month == 1) ? year - 1 : year;
        Integer lastMonthTotal = mapper.getMonthlyTotal(vendorId, prevYear, prevMonth);
        result.put("lastMonthTotal", lastMonthTotal == null ? 0 : lastMonthTotal);

        return result;
    }


}
