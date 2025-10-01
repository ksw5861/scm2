package com.yedam.scm.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.scm.supplier.service.SupplierService;
import com.yedam.scm.vo.PurchaseMatVO;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/supplier")
@RequiredArgsConstructor
public class SupplierController {

    final SupplierService service;

    //자재주문목록
    @GetMapping("/OrderList/{vendorId}")
    public List<PurchaseMatVO> getMatOerderList(@PathVariable String vendorId) {
        return service.getMatOerderList(vendorId);
    }
    
    //자재주문승인
    @PostMapping("/approve")
    public int updateOrderApprove(@RequestBody Map<String, Object> data) {
        return service.updateOrderApprove(data);
    }
    
    //출고대기목록(출고지시등록페이지 로드시 출력되는 목록)
    @GetMapping("/releaseList/{vendorId}")
    public List<PurchaseMatVO> getMatWReleaseList (@PathVariable String vendorId) {
        return service. getMatWReleaseList(vendorId);
    }
    
    //출고지시
    @PostMapping("/shipMaterial")
    public void insertReleaseData(@RequestBody List<PurchaseMatVO> payload) {
         service.insertReleaseData(payload);
    }

}
