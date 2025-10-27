package com.yedam.scm.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.scm.dto.ApproveMatSearchDTO;
import com.yedam.scm.dto.matSupplySearchDTO;
import com.yedam.scm.supplier.service.SupplierService;
import com.yedam.scm.vo.InboundLogVO;
import com.yedam.scm.vo.InboundVO;
import com.yedam.scm.vo.PurchaseMatVO;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("${api.base-path:/}")
@RequiredArgsConstructor
public class SupplierController {

    final SupplierService service;

    //발주목록
    @GetMapping("/sOrderList/{vendorId}")
    public List<PurchaseMatVO> getMatOerderList(@PathVariable String vendorId, ApproveMatSearchDTO searchDTO) {
        return service.getMatOerderList(vendorId, searchDTO);
    }
    
    //발주승인처리
    @PostMapping("/sapprove")
    public int updateOrderApprove(@RequestBody Map<String, Object> data) {
        return service.updateOrderApprove(data);
    }
    
    //발주반려
    @PostMapping("/sreject")
    public void updateOrderReject(@RequestParam Long purId, @RequestParam String rejMemo, @RequestParam String staff ){
        service.updateOrderReject(purId, rejMemo, staff);
    }

    //출고승인된목록(출고지시등록페이지 로드시 출력되는 목록)
    @GetMapping("/sreleaseList/{vendorId}")
    public List<PurchaseMatVO> getMatWReleaseList (@PathVariable String vendorId, matSupplySearchDTO searchDTO) {
        return service. getMatWReleaseList(vendorId, searchDTO);
    }
    
    //출고지시(부분출고/전량출고 + 출고예정일)
    @PostMapping("/sshipMaterial")
    public void insertReleaseData(@RequestBody List<PurchaseMatVO> payload) {
         service.insertReleaseData(payload);
    }
    
    //출고등록화면
    //1) 출고승인목록
    @GetMapping("/sApprovedList/{vendorId}")
    public List<PurchaseMatVO> getApprovedShipmentList (@PathVariable String vendorId, ApproveMatSearchDTO searchDTO) {
        return service.getApprovedShipmentList(vendorId, searchDTO);
    }
    
    //2)출고등록
    /*
     1. 입고마스터테이블 seq -> 입고마스터 insert / 출고정보 insert 
     2. 입고상세insert / 입고이력 insert / 기존발주테이블 요청수량 = 누적출고수량은 종결처리
    */
    @PostMapping("/sshipment")
    public void insertShipmentInfo(@RequestBody InboundVO MatShipInfo) {       
        service.insertShipmentInfo(MatShipInfo);
    }
    
    //출고(공급)목록
    @GetMapping("/ssupplyList/{vendorId}")
    public List<InboundVO> getSupplyList(@PathVariable String vendorId, matSupplySearchDTO searchDTO) {
        return service.getSupplyList(vendorId, searchDTO);
    }
    //출고상세목록
    @GetMapping("/ssupplyDetailList")
    public List<InboundLogVO> getSupplyDetailList(@RequestParam Long inboundDetId) {
        return service.getSupplyDetailList(inboundDetId);
    }
    //대시보드
    @GetMapping("/sdashboard/{vendorId}")
    public Map<String, Object> getVendorDashboard(@PathVariable String vendorId) {
        return service.getVendorDashboard(vendorId);
    }
    //주문내역조회
    @GetMapping("/requestResult/{vendorId}")
    public List<PurchaseMatVO> getRequestResultList(@PathVariable String vendorId, matSupplySearchDTO searchDTO) {
        return service.getRequestResultList(vendorId, searchDTO);
    }
}

