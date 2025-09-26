package com.yedam.scm.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.scm.dto.InboundListRes;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.dto.WarehouseListRes;
import com.yedam.scm.product.service.InboundService;
import com.yedam.scm.vo.ItemInboundVO;

import lombok.RequiredArgsConstructor;



@RestController
@RequiredArgsConstructor
public class DwController {


    
    private final InboundService service;

    // 목록조회
    @GetMapping("/lots")
    public List<ItemInboundVO> getInboundLots(ItemInboundVO vo) {
        return service.selectInboundLots(vo);
    }

    // 등록
    @PostMapping("/lots")
    public Map<String, Object> insertInbound(@RequestBody Map<String, Object> vo) {

        Map<String, Object> response = new HashMap<>();

        if(service.registerInbound(vo)) {
            response.put("status", "success");
            return response;
        } else {
            response.put("status", "fail");
            return response;
        }
        // axios.post => {"status" : "success/fail"}
    }

    // 삭제
    @DeleteMapping("/lots/{inboundId}")
    public int deleteInbound(@PathVariable String inboundId) {
        return service.deleteInbound(inboundId);
    }



    // 입고처리
@PostMapping("/inbound")
public ResponseEntity<Map<String, Object>> registerInbound(@RequestBody Map<String, Object> inbound) {
    boolean isRegister = service.registerInbound(inbound);
    Map<String, Object> response = new HashMap<>();
    if (isRegister) {
        response.put("retCode", "success");
        return ResponseEntity.ok(response);
    } else {
        response.put("retCode", "BAD_REQUEST");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}

// ================================
// 모달 검색 API (제품코드, 제품명, 생산일자)
// ================================
@GetMapping("/inbound/product")
public InboundListRes getInboundProducts(
    @RequestParam(defaultValue = "1") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(required = false) String keyword // EMP000, ~~원두
) {
    // 페이징 세팅
    PageDTO paging = new PageDTO(page, size);

    // 서비스 호출 후 반환
    return service.getInboundProductList(keyword, paging);
}


        @GetMapping("/inbound/warehouse")
    public WarehouseListRes getWarehouses(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String keyword
    ) {
        PageDTO paging = new PageDTO(page, size);
        return service.getWarehouseList(keyword, paging);
    }







    

    } // end
    
    // // 입고처리
    // @PostMapping("/inbound")
    // public Map<String, Object> registerInbound(@RequestBody Map<String, Object> inbound) {
        
    //     boolean isRegister = service.registerInbound(inbound);

    //     Map<String, Object> result = new HashMap<>();
    //     if (isRegister) {
    //         result.put("status", "success");
    //         return result;
    //     } else {
    //         result.put("status", "fail");
    //         return result;
    //     }
    // }




