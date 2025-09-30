package com.yedam.scm.supplier.service.impl;

import java.beans.Transient;
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

    //출고대기목록
    @Override
    public List<PurchaseMatVO> getMatWReleaseList(String vendorId) {
        return mapper.getMatWReleaseList(vendorId);
    }

    //출고등록
    @Transactional
    @Override
    public void insertReleaseData(List<PurchaseMatVO> payload) {
       //1) purId: row.id, /matId: row.matId, /orderNo: row.purNo, /outQty: row.outQty, /vendorId: vendorId.value 
        
       //자재구매요청 상태값 업데이트
        for(PurchaseMatVO row : payload) {
            
            Long purId   = row.getPurId();
            String matId = row.getMatId();
            String purNo = row.getPurNo(); //발주번호 = 주문번호
            Long outQty = row.getPurStatusLogVO().getSupOutQty(); //이력관리VO에 있음.
            String vendorId = row.getVendorId();

            //purchase_mat테이블 상태: 부분출고/전량출고로 상태값 변경 + 누적출고수량 updqate
            //이력테이블 이력, venderId, purID, 출고수량 입력.
            mapper.callReleaseMatPoc(purId, outQty, vendorId);
        }


       //프로시저 쓴다면??????? 1) 자재구매 테이블 업데이트 + 이력 insert 
       //=============================================
       //2) 마스터 inert 후 입고테이블 아이디 받아오고
       // -> 받은 입고테이블아이디로 상세정보(for)로  
    }
    
}
