package com.yedam.scm.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.yedam.scm.product.service.InboundService;
import com.yedam.scm.vo.ItemInboundVO;

@RestController
@RequestMapping("/api/inbound")
@RequiredArgsConstructor
public class DwController {

    private final InboundService inboundService;

    // LOT 목록 조회
    @GetMapping("/lots")
    public List<ItemInboundVO> selectInboundLots(
            @RequestParam(required = false) String prodCode,
            @RequestParam(required = false) String prodName,
            @RequestParam(required = false) String makerDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Map<String,Object> params = new HashMap<>();
        params.put("prodCode", prodCode);
        params.put("prodName", prodName);
        params.put("makerDate", makerDate);
        params.put("offset", (page - 1) * size);
        params.put("limit", size);

        return inboundService.selectInboundLots(params);
    }

    // 입고 등록
    @PostMapping
    public int insertInbound(@RequestBody ItemInboundVO vo) {
        return inboundService.insertInbound(vo);
    }

    // 입고 삭제
    // @DeleteMapping("/{inboundId}")
    // public int deleteInbound(@PathVariable String inboundId) {
    //     return inboundService.deleteInbound(inboundId);
    // }
}
