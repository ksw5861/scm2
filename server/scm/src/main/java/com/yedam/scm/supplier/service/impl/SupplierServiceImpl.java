package com.yedam.scm.supplier.service.impl;

import java.beans.Transient;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.scm.supplier.mapper.SupplierMapper;
import com.yedam.scm.supplier.service.SupplierService;
import com.yedam.scm.vo.PurchaseMatVO;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class SupplierServiceImpl implements SupplierService  {

    final SupplierMapper mapper;

    //주문목록
    @Override
    public List<PurchaseMatVO> getMatOerderList(String vendorId) {
        return mapper.getMatOerderList(vendorId);
    }

    //주문승인
    @Transactional
    @Override
    public int updateOrderApprove(Map<String, Object> data) {
       
        List<Integer> idList  = (List<Integer>) data.get("purId");
        String name = (String) data.get("name");

        int updatedCount = 0;

        System.out.println("리스트용" + idList);
        System.out.println("이름이세요" + name);


        for(Integer purId : idList ){
            updatedCount += mapper.updateOrderApprove(purId);
            mapper.insertStatusLog(purId, name);
        }
        return updatedCount;
    }

    //출고지시대기목록
    @Override
    public List<PurchaseMatVO> getMatWReleaseList(String vendorId) {
        return mapper.getMatWReleaseList(vendorId);
    }

    //출고등록
    @Transactional
    @Override
    public void insertReleaseData(List<PurchaseMatVO> payload) {
       //payload담긴 값: purId: row.id, /matId: row.matId, /orderNo: row.purNo, /outQty: row.outQty, /vendorId: vendorId.value 
       // 1) urchase_mat테이블 상태: 부분출고/전량출고로 상태값 변경 + 누적출고수량 updqate
       // 2) 이력테이블 이력, venderId, purID, 출고수량 입력. 

        String vendorId = null;
       
       //자재구매요청 상태값 업데이트
        for(PurchaseMatVO row : payload) {
            
            Long purId   = row.getPurId();
            Long outQty = row.getPurStatusLogVO().getSupOutQty(); //이력관리VO에 있음.[메인은 누적출고량만 기록하기때문에 이력등록시 매번 출고수량등록]
            vendorId = row.getVendorId();
            Date expectDate =row.getExpectDate();

            mapper.callReleaseMatPoc(purId, outQty, vendorId, expectDate);
        }

    }
    
}


// //입고마스터 PK가지고 옴. 
//         Long inboundMasterPK = mapper.getInboundMasterPK(); 
        
//         //1) 마스터테이블 isert [PK/verderId]
//         mapper.insertInboundMaster(inboundMasterPK, vendorId);

//         //2) 디테일테이블 (for순환)[입고마스터PK/발주아이디/출고수량/제품코드/vnderId]
//         for(PurchaseMatVO row : payload) {
            
//             Long purId   = row.getPurId();
//             String matId = row.getMatId();
//             //String purNo = row.getPurNo(); //발주번호 = 주문번호
//             Long outQty = row.getPurStatusLogVO().getSupOutQty(); //이력관리VO에 있음.
//             vendorId = row.getVendorId();

//             mapper.insertInboundDetail(inboundMasterPK, purId, matId, outQty, vendorId);

//         }