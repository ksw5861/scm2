package com.yedam.scm.purchaseMat.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;


import com.yedam.scm.vo.PrdPlanDetailVO;
import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.ProductionPlanVO;

@Mapper
public interface PurchaseMatMapper {
    
    int insertProductionPlan(ProductionPlanVO master);
    int insertProductionPlanDetail(PrdPlanDetailVO detail);


   
    /*==========================
     * 드롭다운/모달용
     ===========================*/
    List<ProductVO> getProductList();
}
