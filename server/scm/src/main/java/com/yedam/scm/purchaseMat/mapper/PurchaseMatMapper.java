package com.yedam.scm.purchaseMat.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.yedam.scm.vo.PrdPlanDetailVO;
import com.yedam.scm.vo.ProductionPlanVO;

/*
 * Mapper Interface
 * - 생산계획 등록
 * - 발주등록
 * - 발주목록
 * - 입고등록 (자재 LOT 생성)
 * - 입고목록
 *  
 */

@Mapper
public interface PurchaseMatMapper {
    int insertProductionPlan(ProductionPlanVO master);
    int insertProductionPlanDetail(PrdPlanDetailVO detail);
}
