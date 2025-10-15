// src/main/java/com/yedam/scm/web/SalesController.java
package com.yedam.scm.web;

import com.yedam.scm.order.service.SalesMarginService;
import com.yedam.scm.vo.SalesMarginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SalesMarginService marginService;

    // 목록 조회 (테이블 바인딩)
    @GetMapping("/margin/list")
    public List<SalesMarginVO> getMarginList() {
        return marginService.getList();
    }

    // 단건 저장(업서트) — 행 하나씩 저장 버튼 클릭용
    @PostMapping("/margin/save")
    public Map<String, Object> saveMargin(@RequestBody SalesMarginVO vo) {
        marginService.save(vo);
        return Map.of("result", "success");
    }

    // 일괄 저장(업서트) — Vue의 rows 전체 저장
    @PostMapping("/margin/save-all")
    public Map<String, Object> saveAll(@RequestBody List<SalesMarginVO> list) {
        marginService.saveAll(list);
        return Map.of("result", "success");
    }

    // 삭제
    @DeleteMapping("/margin/{saleProdId}")
    public Map<String, Object> deleteMargin(@PathVariable String saleProdId) {
        marginService.delete(saleProdId);
        return Map.of("result", "success");
    }
}
