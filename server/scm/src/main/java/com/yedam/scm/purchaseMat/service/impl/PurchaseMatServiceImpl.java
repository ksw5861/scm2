package com.yedam.scm.purchaseMat.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.scm.purchaseMat.mapper.PurchaseMatMapper;
import com.yedam.scm.purchaseMat.service.PurchaseMatService;
import com.yedam.scm.vo.PrdPlanDetailVO;
import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.ProductionPlanVO;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PurchaseMatServiceImpl implements PurchaseMatService{
    
    final PurchaseMatMapper purchaseMatMapper; //mapper injection

    //생산계획 등록
    @Transactional
    public int insertProductionPlan(ProductionPlanVO plan) {
        // 1. 마스터 저장
       int result = purchaseMatMapper.insertProductionPlan(plan); //mapper 호출하고, plan에 planId의 값이 세팅됨
        System.out.println("생산계획 등록 후 planId : " + plan.getPlId());

        // 2. 디테일 저장
        if (plan.getPrdPlanDetailList() != null) {  //생산계획에 디테일이 있으면 
            for (PrdPlanDetailVO details : plan.getPrdPlanDetailList()) {
                details.setPlId(plan.getPlId()); // FK 세팅
                purchaseMatMapper.insertProductionPlanDetail(details);
            }
        }
    return result; 
    }

    /*==========================
     * 모달용
     ===========================*/
    //제품모달(생산계획등록시)
    public int getProductCount(String prodName) {
        return purchaseMatMapper.getProductCount(prodName);
    }

    public List<ProductVO> getProductList(String prodName, int offset, int size) {
        return purchaseMatMapper.getProductList(prodName, offset, size);
    }
}
