package com.yedam.scm.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.yedam.scm.order.service.SalesMarginService;
import com.yedam.scm.vo.SalesDetailVO;
import com.yedam.scm.vo.SalesMarginVO;
import com.yedam.scm.vo.SalesMasterVO;
import com.yedam.scm.vo.SalesOrderVO;

@RestController
@RequestMapping("${api.base-path:/}")
public class SalesMarginController {

    @Autowired
    private SalesMarginService service;

    // 목록 조회
    @GetMapping("/sales/margin/list")
     public List<SalesMarginVO> getList(@RequestParam String vendorId) {
     return service.getList(vendorId);
     }

    // 단건 조회
    @GetMapping("/sales/margin/{id}")
    public SalesMarginVO getById(
        @RequestParam String vendorId,    
        @PathVariable("id") String id
    ) {
        return service.getById(vendorId, id);  
    }


    // 여러 건 저장(기존데이터삭제+새로저장) (save-all)
    @PostMapping("/sales/margin/save-all")
    public ResponseEntity<?> saveAll(@RequestBody List<SalesMarginVO> list) {
        service.saveAll(list);
        return ResponseEntity.ok("All saved successfully");
    }

    // 삭제 - 목록행에서 휴지통버튼
    @DeleteMapping("/sales/margin/{id}")
    public ResponseEntity<?> delete(
        @RequestParam String vendorId,      
        @PathVariable("id") String id      
    ) {
        service.delete(vendorId, id);      
        return ResponseEntity.ok("Deleted successfully");
    }

    //포스기 화면에서 결제등록
    @PostMapping("/sales/register")
    public ResponseEntity<String> registerSale(@RequestBody SalesMasterVO vo) {
        try {
            service.registerSale(vo);
            return ResponseEntity.ok("Sale registered successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to register sale");
        }
    }


    // 매출이력조회
    @GetMapping("/sales/history")
    public List<SalesMasterVO> getSalesHistory(@RequestParam String vendorId) {
       return service.getSalesHistory(vendorId);
    }

    // 📌 매출 요약 (오늘 vs 어제) 매출내역탭에 어제날짜와 증감대비를 위한것
    @GetMapping("/sales/daily-summary")
    public ResponseEntity<Map<String, Object>> getDailySummary(@RequestParam String vendorId) {
        Map<String, Object> result = service.getDailySummary(vendorId);
        return ResponseEntity.ok(result);
    }

    // ✅ 월별 매출 탭에 요약칸
    @GetMapping("/sales/monthly-summary")
    public Map<String, Object> getMonthlySummary(
            @RequestParam String vendorId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return service.getMonthlySummary(vendorId, year, month);
    }
//-----------------------------------------------------대시보드--------------------------------------------------------


    // ✅ 대시보드용 매출 추이 그래프 (일별/월별)
@GetMapping("/branch/salestrend")
public ResponseEntity<List<Map<String, Object>>> getSalesTrend(
        @RequestParam String vendorId,
        @RequestParam(defaultValue = "daily") String range) {

    List<Map<String, Object>> list = service.getSalesTrend(vendorId, range);
    return ResponseEntity.ok(list);
}

// 📊 작년 vs 올해 매출 비교 (일별/월별 선택 가능)
@GetMapping("/branch/salescompare")
public ResponseEntity<Map<String, Object>> getSalesCompare(
        @RequestParam String vendorId,
        @RequestParam String range   // ✅ 일별(daily) 또는 월별(monthly)
) {
    Map<String, Object> result = service.getSalesCompare(vendorId, range);
    return ResponseEntity.ok(result);
}


// ✅ 원두 랭킹
@GetMapping("/branch/coffeerank")
public ResponseEntity<List<Map<String,Object>>> getCoffeeRank(
        @RequestParam String vendorId,
        @RequestParam(defaultValue = "monthly") String range) {
    return ResponseEntity.ok(service.getCoffeeRank(vendorId, range));
}

// 매출 성장률 (오늘vs어제, 이번달vs지난달, 올해vs작년)
@GetMapping("/branch/sales-growth")
public Map<String, Object> getSalesGrowth(@RequestParam String vendorId) {
    return service.getSalesGrowth(vendorId);
}

// ✅ 결제수단별 매출 비중
@GetMapping("/branch/paymethod")
    public List<Map<String, Object>> getPayMethod(
            @RequestParam String vendorId,
            @RequestParam(defaultValue = "daily") String range
    ) {
        return service.getPayMethod(vendorId, range);
    }

// 대시보드에 1일부터 말일까지 출고완료,배송완료에 결제대기인건, 15일자 기준 미수금 계산
@GetMapping("/sales/next-due-amount")
    public SalesOrderVO getNextDueAmount(@RequestParam String vendorId) {
        return service.getNextDueAmount(vendorId);
    }

// 대시보드에 여신한도 잔액
@GetMapping("/sales/finance-summary")
    public SalesOrderVO getSalesFinanceSummary(@RequestParam String vendorId) {
        return service.getSalesFinanceSummary(vendorId);
    }




}
