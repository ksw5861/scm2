package com.yedam.scm.purchaseMat.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.scm.purchaseMat.mapper.PurchaseMatMapper;
import com.yedam.scm.purchaseMat.service.PurchaseMatService;
import com.yedam.scm.vo.MatVendorVO;
import com.yedam.scm.vo.MrpDetailVO;
import com.yedam.scm.vo.PrdPlanDetailVO;
import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.ProductionPlanVO;
import com.yedam.scm.vo.PurStatusLogVO;
import com.yedam.scm.vo.PurchaseMatVO;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PurchaseMatServiceImpl implements PurchaseMatService{
    
    final PurchaseMatMapper mapper; //mapper injection

    //생산계획 등록
    @Transactional
    public int insertProductionPlan(ProductionPlanVO plan) {
        // 1. 마스터 저장
       int result =  mapper.insertProductionPlan(plan); //mapper 호출하고, plan에 planId의 값이 세팅됨
        System.out.println("생산계획 등록 후 planId : " + plan.getPlId());

        // 2. 디테일 저장
        if (plan.getPrdPlanDetailList() != null) {  //생산계획에 디테일이 있으면 
            for (PrdPlanDetailVO details : plan.getPrdPlanDetailList()) {
                details.setPlId(plan.getPlId()); // FK 세팅
                 mapper.insertProductionPlanDetail(details);
            }
        }
    return result; 
    }
    
    //생산계획목록
    @Override
    public List<ProductionPlanVO> getPlanMasterList(){
        return mapper.getPlanMasterList();
    };

    @Override
    public List<PrdPlanDetailVO> getPlanList(){
        return mapper.getPlanList();
    }
    
    //mrp목록
    @Override
    public List<MrpDetailVO> getMrpDetailList(){
        return mapper.getMrpDetailList();
    }
    
    //자재별공급처
    @Override
    public List<MatVendorVO> getMatVendorList(String matId) {
        return mapper.getMatVendorList(matId);
    }

    //자재주문등록
    @Override
    @Transactional
    public boolean callReqestMatProc(List<PurchaseMatVO> requestList) {
        try {
            for (PurchaseMatVO list : requestList) { // mpper.xml에서 (<select>로 procedure call)
                mapper.callReqestMatProc(list);
            }
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //자재주문목록
    @Override
    public List<PurchaseMatVO> getPurchaseList() {
       return mapper.getPurchaseList();
    }

    //자재주문상태
    @Override
    public List<PurStatusLogVO> getPurchaseStatus(Long purId) {
       return mapper.getPurchaseStatus(purId);
    }



    /*==========================
     * 드롭다운/모달용
     ===========================*/
    //제품리스트
    @Override
    public List<ProductVO> getProductList() {
        return mapper.getProductList();
    }

   
}