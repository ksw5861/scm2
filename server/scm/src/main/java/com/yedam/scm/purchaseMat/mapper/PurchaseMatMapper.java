package com.yedam.scm.purchaseMat.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;


import com.yedam.scm.vo.PrdPlanDetailVO;
import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.ProductionPlanVO;

@Mapper
public interface PurchaseMatMapper {
    
    //생산계획등록[마스터 + 디테일]
    int insertProductionPlan(ProductionPlanVO master); //마스터
    int insertProductionPlanDetail(PrdPlanDetailVO detail); //디테일

    //제품생산계획리스트
    List<PrdPlanDetailVO> getPlanList();

   
    /*==========================
     * 드롭다운/모달용
     ===========================*/
    List<ProductVO> getProductList();
}
