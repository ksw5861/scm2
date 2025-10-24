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
import com.yedam.scm.vo.SalesOrderVO;

@Service
public class SalesMarginServiceImpl implements SalesMarginService {

    @Autowired
    private SalesMarginMapper mapper;

    @Override
    public List<SalesMarginVO> getList(String vendorId) {
     return mapper.selectSalesMarginList(vendorId);
    }

    @Override
    public SalesMarginVO getById(String vendorId, String saleProdId) {
     return mapper.selectSalesMarginById(vendorId, saleProdId);
    }

    @Transactional
    @Override
    public int saveAll(List<SalesMarginVO> list) {
        if (list == null || list.isEmpty()) return 0;

        // ✅ list 첫 번째 데이터에서 vendorId 추출해서 넣어주면 OK
        String vendorId = list.get(0).getVendorId();
        mapper.deleteAll(vendorId);   
        int insertedCount = 0;
        for (SalesMarginVO vo : list) {
            insertedCount += mapper.insert(vo);
        }
        return insertedCount;
    }

    @Override
    public int delete(String vendorId, String saleProdId) {
        return mapper.deleteSalesMargin(vendorId, saleProdId);
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
@Override
public List<Map<String, Object>> getSalesTrend(String vendorId, String range) {
    if ("monthly".equalsIgnoreCase(range)) {
        return mapper.getSalesTrendMonthly(vendorId, range); // ✅ range까지 전달
    } else {
        return mapper.getSalesTrendDaily(vendorId, range);   // ✅ range까지 전달
    }
}

@Override
public Map<String, Object> getSalesCompare(String vendorId, String range) {
    Map<String, Object> result = new HashMap<>();
    result.put("compareData", mapper.getSalesCompare(vendorId, range));
    return result;
}

@Override
public List<Map<String, Object>> getCoffeeRank(String vendorId, String range) {
    return mapper.getCoffeeRank(vendorId, range);
}

// 매출 성장률
@Override
public Map<String, Object> getSalesGrowth(String vendorId) {
    Map<String, Object> result = new HashMap<>();
    Map<String, Object> growth = mapper.selectSalesGrowth(vendorId);

    if (growth != null) {
        double dailyRate = calcGrowth(growth.get("TODAY"), growth.get("YESTERDAY"));
        double monthlyRate = calcGrowth(growth.get("CURR_MONTH"), growth.get("PREV_MONTH"));
        double yoyRate = calcGrowth(growth.get("THIS_YEAR"), growth.get("LAST_YEAR"));

        result.put("dailyRate", dailyRate);
        result.put("monthlyRate", monthlyRate);
        result.put("yoyRate", yoyRate);
    } else {
        result.put("dailyRate", 0);
        result.put("monthlyRate", 0);
        result.put("yoyRate", 0);
    }
    return result;
}

// ✅ 이거는 Impl에만 private 메서드로!
private double calcGrowth(Object curr, Object prev) {
    double c = curr == null ? 0 : Double.parseDouble(curr.toString());
    double p = prev == null ? 0 : Double.parseDouble(prev.toString());
    if (p == 0) return 0;
    return Math.round(((c - p) / p * 100) * 10) / 10.0;
}

   // ✅ 결제수단별 매출 조회 (CARD / CASH)
    
    @Override
    public List<Map<String, Object>> getPayMethod(String vendorId, String range) {
        return mapper.selectPayMethod(vendorId, range);
    }


    // 대시보드에 1일부터 말일까지 출고완료,배송완료에 결제대기인건, 15일자 기준 미수금 계산
    @Override
    public SalesOrderVO getNextDueAmount(String vendorId) {
        return mapper.selectNextDueAmount(vendorId);
    }

    // 대시보드에 여신한도 잔액
     @Override
    public SalesOrderVO getSalesFinanceSummary(String vendorId) {
        return mapper.selectSalesFinanceSummary(vendorId);
    }


}
