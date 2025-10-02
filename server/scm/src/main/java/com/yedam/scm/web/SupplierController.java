package com.yedam.scm.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.scm.supplier.service.SupplierService;
import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.PurchaseMatVO;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


/*
 * 출고지시: 출고수량 - 출고예정일 insert//purchase_mat테이블 
[부분출고/전량출고] 상태값 변경



출고등록: 부분출고/전량출고에 대한 값만 받아옴. 
-> 입고테이블 데이터 insert
 
1) 입고마스터아이디 seq
2) 출고번호 + 출고일 + 입고대기(상태값 m1) + 공급처코드
3) 출고정보: 입고아이디 + 운송업체 + 운송번호 + 차량번호 + 도착지

4) for문 돌면서 
자재코드 + 출고수량 + 공급처코드 + 상태'입고대기 d1'


5) purchase_status_log ==> 
출고시 해당 출고상세아이디는 Y로 상태변경. 

6) pur_id 의 상태값이 전량출고일때 ! purchase_mat=> 종결처리 
전량출고는 종결로 update  해당 출고번호건 (출고번호 = 출고상세 아이디값 1:1로 매칭됨.)

*/







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
    
    //1) 출고승인목록
    @GetMapping("/ApprovedList/{vendorId}")
    public List<PurchaseMatVO> getApprovedShipmentList (@PathVariable String vendorId) {
        return service.getApprovedShipmentList(vendorId);
    }
    
    //출고등록  [1)출고승인목록 출력 2)입고마스터테이블 INSERT 3)입고마스터테이블데이터insert 3)입고상세테이블insert  4)배송정보테이블insert]





    //출고등록 [ 2)입고마스터테이블 INSERT 3)입고마스터테이블데이터insert 3)입고상세테이블insert  4)배송정보테이블insert]
    // @PostMapping("/shipment")
    // public String postMethodName(@RequestBody String entity) {
    //     //TODO: process POST request
        
    //     return entity;
    // }
    
}



/*
 * 
출고등록: 부분출고/전량출고에 대한 값만 받아옴. 
-> 입고테이블 데이터 insert
 
1) 입고마스터아이디 seq
2) 출고번호 + 출고일 + 입고대기(상태값 m1) + 공급처코드
3) 출고정보: 입고아이디 + 운송업체 + 운송번호 + 차량번호 + 도착지

4) for문 돌면서 
자재코드 + 출고수량 + 공급처코드 + 상태'입고대기 d1'


5) purchase_status_log ==> 
출고시 해당 출고상세아이디는 Y로 상태변경. 

6) pur_id 의 상태값이 전량출고일때 ! purchase_mat=> 종결처리 
전량출고는 종결로 update  해당 출고번호건 (출고번호 = 출고상세 아이디값 1:1로 매칭됨.) 
 * 
*/
