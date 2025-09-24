package com.yedam.scm.purchaseMat.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.yedam.scm.vo.PrdPlanDetailVO;
import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.ProductionPlanVO;

@Mapper
public interface PurchaseMatMapper {
    
    int insertProductionPlan(ProductionPlanVO master);
    int insertProductionPlanDetail(PrdPlanDetailVO detail);


   
    /*==========================
     * 모달용
     ===========================*/
      int getProductCount(@Param("prodName") String prodName);

    List<ProductVO> getProductList(@Param("prodName") String prodName,
                                   @Param("offset") int offset,
                                   @Param("size") int size);
}
