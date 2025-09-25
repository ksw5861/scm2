package com.yedam.scm.purchaseMat.service;

import java.util.List;

import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.ProductionPlanVO;

public interface PurchaseMatService {
    int insertProductionPlan(ProductionPlanVO plan); //생산계획등록 [마스터 + 디테일 한번에]

     /*=========================
      드롭다운/모달용
     =========================*/
     //제품리스트
    List<ProductVO> getProductList();
}
