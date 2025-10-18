package com.yedam.scm.web;

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

@RestController
@RequestMapping
public class SalesMarginController {

    @Autowired
    private SalesMarginService service;

    // 목록 조회
    @GetMapping("/sales/margin/list")
    public List<SalesMarginVO> getList() {
        return service.getList();
    }

    // 단건 조회
    @GetMapping("/sales/margin/{id}")
    public SalesMarginVO getById(@PathVariable("id") String id) {
        return service.getById(id);
    }


    // 여러 건 저장(기존데이터삭제+새로저장) (save-all)
    @PostMapping("/sales/margin/save-all")
    public ResponseEntity<?> saveAll(@RequestBody List<SalesMarginVO> list) {
        service.saveAll(list);
        return ResponseEntity.ok("All saved successfully");
    }

    // 삭제 - 목록행에서 휴지통버튼
    @DeleteMapping("/sales/margin/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") String id) {
        service.delete(id);
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

}
