package com.yedam.scm.purchaseMat.service;

import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.yedam.scm.vo.MatVendorVO;
import com.yedam.scm.vo.MrpDetailVO;
import com.yedam.scm.vo.PrdPlanDetailVO;
import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.ProductionPlanVO;
import com.yedam.scm.vo.PurStatusLogVO;
import com.yedam.scm.vo.PurchaseMatVO;
import com.yedam.scm.vo.WareHouseVO;

public interface PurchaseMatService {
    //생산계획등록 [마스터 + 디테일 한번에]
    int insertProductionPlan(ProductionPlanVO plan); 
    //생산계획목록출력
    List<ProductionPlanVO> getPlanMasterList();
    List<PrdPlanDetailVO> getPlanList();
    //mrp산출목록
    List<MrpDetailVO> getMrpDetailList();
    //자재주문등록
    void callReqestMatProc(List<PurchaseMatVO> requestList);
    //자재주문목록
    List<PurchaseMatVO> getPurchaseList();
    //자재주문상태
     List<PurStatusLogVO> getPurchaseStatus(Long purId);
     /*=========================
      드롭다운/모달용
     =========================*/
    //제품리스트
    List<ProductVO> getProductList();
    //자재별공급처리스트
    List<MatVendorVO>getMatVendorList(String matId);
    //창고리스트
    List<WareHouseVO> getWarehouseList();
}
