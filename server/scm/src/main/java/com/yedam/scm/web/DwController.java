package com.yedam.scm.web;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import com.yedam.scm.product.service.InboundService;
import com.yedam.scm.vo.ProductLotVO;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DwController {

    private final InboundService service;

    // LOT 목록 조회
    @GetMapping("/lots")
    public List<ProductLotVO> getLots(ProductLotVO vo) {
        return service.selectInboundLots(vo);
    }

    // 입고 등록 (업데이트)
    @PostMapping("/inbound")
    public int inbound(@RequestBody ProductLotVO vo, HttpSession session) {
        // 로그인 세션에서 담당자 가져오기 (예: loginId 세션키)
        String loginUser = (String) session.getAttribute("loginId");
        vo.setStatus("입고완료");
        // employeeId를 VO에 추가하고 싶다면 컬럼/필드 확장 필요
        return service.updateInbound(vo);
    }

    // 입고 삭제
    @DeleteMapping("/inbound/{prdLot}")
    public int remove(@PathVariable String prdLot) {
        return service.deleteInbound(prdLot);
    }

}

// 입고처리
// @PostMapping("/inbound")
// public ResponseEntity<Map<String, Object>> registerInbound(@RequestBody
// Map<String, Object> inbound) {

// boolean isRegister = service.registerInbound(inbound);

// Map<String, Object> response = new HashMap<>();
// if (isRegister) {
// response.put("status", "success");
// return ResponseEntity.ok(response);
// } else {
// response.put("status", "not_found");
// return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
// }

// // 입고처리
// @PostMapping("/inbound")
// public Map<String, Object> registerInbound(@RequestBody Map<String, Object>
// inbound) {

// boolean isRegister = service.registerInbound(inbound);

// Map<String, Object> result = new HashMap<>();
// if (isRegister) {
// result.put("status", "success");
// return result;
// } else {
// result.put("status", "fail");
// return result;
// }
// }
