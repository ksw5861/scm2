package com.yedam.scm.web;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.yedam.scm.order.service.SalesMarginService;
import com.yedam.scm.vo.SalesMarginVO;

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

    // 단건 저장 (MERGE)
    @PostMapping("/sales/margin/save")
    public ResponseEntity<?> save(@RequestBody SalesMarginVO vo) {
        service.save(vo);
        return ResponseEntity.ok("Saved successfully");
    }

    // 여러 건 저장 (save-all)
    @PostMapping("/sales/margin/save-all")
    public ResponseEntity<?> saveAll(@RequestBody List<SalesMarginVO> list) {
        service.saveAll(list);
        return ResponseEntity.ok("All saved successfully");
    }

    // 삭제
    @DeleteMapping("/sales/margin/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") String id) {
        service.delete(id);
        return ResponseEntity.ok("Deleted successfully");
    }
}
