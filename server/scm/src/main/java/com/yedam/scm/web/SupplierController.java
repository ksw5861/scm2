package com.yedam.scm.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.scm.supplier.service.SupplierService;
import com.yedam.scm.vo.InboundVO;
import com.yedam.scm.vo.ProductVO;
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

    //주문목록
    @GetMapping("/OrderList/{vendorId}")
    public List<PurchaseMatVO> getMatOerderList(@PathVariable String vendorId) {
        return service.getMatOerderList(vendorId);
    }
    
    //주문승인처리
    @PostMapping("/approve")
    public int updateOrderApprove(@RequestBody Map<String, Object> data) {
        return service.updateOrderApprove(data);
    }
    
    //출고승인된목록(출고지시등록페이지 로드시 출력되는 목록)
    @GetMapping("/releaseList/{vendorId}")
    public List<PurchaseMatVO> getMatWReleaseList (@PathVariable String vendorId) {
        return service. getMatWReleaseList(vendorId);
    }
    
    //출고지시(부분출고/전량출고 + 출고예정일)
    @PostMapping("/shipMaterial")
    public void insertReleaseData(@RequestBody List<PurchaseMatVO> payload) {
         service.insertReleaseData(payload);
    }
    
    //출고등록화면
    //1) 출고승인목록
    @GetMapping("/ApprovedList/{vendorId}")
    public List<PurchaseMatVO> getApprovedShipmentList (@PathVariable String vendorId) {
        return service.getApprovedShipmentList(vendorId);
    }
    
    //2)출고등록
    /*
     1. 입고마스터테이블 seq -> 입고마스터 insert / 출고정보 insert 
     2.  입고상세 seq -> 입고상세insert / 입고이력 insert / 기존발주테이블 요청수량 = 누적출고수량은 종결처리
    */
    @PostMapping("/shipment")
    public void insertShipmentInfo(@RequestBody InboundVO MatShipInfo) {       
        service.insertShipmentInfo(MatShipInfo);
    }
    
}

/*
======== 출고등록시
입고M
- 출고일 [sysdate]
- 공급처 코드
- 출고번호(생성)
- 입고대기로 상태값

출고정보
- 입고M아이디
- 운송업체
- 차량번호
- 출발지?? 거래처에서 선택해서 주소들어가는걸로 
- 하차지 (공장코드)

입고상세
- 입고M 아이디
- 출고수량
- 공급처코드
- 자재코드
- 상태(입고대기)
- 발주테이블 아이디

입고이력관리
- 입고상세아이디
- 상태값(입고대기)
- 상태변경일[sysdate]

===기존 발주테이블에
상태값 종결처리

 */