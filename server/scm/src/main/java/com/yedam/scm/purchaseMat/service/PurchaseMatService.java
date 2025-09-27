package com.yedam.scm.purchaseMat.service;

import java.util.List;

import com.yedam.scm.vo.MatVendorVO;
import com.yedam.scm.vo.MrpDetailVO;
import com.yedam.scm.vo.PrdPlanDetailVO;
import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.ProductionPlanVO;

public interface PurchaseMatService {
    //생산계획등록 [마스터 + 디테일 한번에]
    int insertProductionPlan(ProductionPlanVO plan); 
    //생산계획목록출력
    List<ProductionPlanVO> getPlanMasterList();
    List<PrdPlanDetailVO> getPlanList();
    //mrp산출목록
    List<MrpDetailVO> getMrpDetailList();
    //자재별공급처
    List<MatVendorVO>getMatVendorList(String matId);

     /*=========================
      드롭다운/모달용
     =========================*/
     //제품리스트
    List<ProductVO> getProductList();
}
