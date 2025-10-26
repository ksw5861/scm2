package com.yedam.scm.purchaseMat.service;

import java.util.List;
import java.util.Map;

import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.dto.PurchaseListSearchDTO;
import com.yedam.scm.dto.prodPlanForAccoDTO;
import com.yedam.scm.dto.purchaseOrderDTO;
import com.yedam.scm.vo.MatStatusVO;
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
    //생산계획마스터(모달)
    List<ProductionPlanVO> getPlanMasterList();
    // 생산계획상세목록
    List<PrdPlanDetailVO> getPlanDetailList(Long plId);
    //mrp산출
    void callCalcMrpProc(Long plId, String empName);
    //mrp산출목록
    List<MrpDetailVO> getMrpList();
    //자재주문등록
    void callReqestMatProc(List<PurchaseMatVO> requestList);
    //자재발주취소
    void purchseCancel(Map<String, Object> data);
    //자재주문목록
    Map<String, Object> getPurchaseList(PurchaseListSearchDTO searchDTO, PageDTO pageDTO);
    //자재주문상태
     List<PurStatusLogVO> getPurchaseStatus(Long purId);
    //발주내역
    List<PurchaseMatVO> getPurchaseOrderList(purchaseOrderDTO searchDTO);
    //생산계획내역
    List<ProductionPlanVO> getPlanlistforAcco(prodPlanForAccoDTO searchDTO);
    //생산계획상세(제품 + MPR) for accodion
    Map<String, Object> getPlanDetailforAcco(Long plId);
    /*=========================
      드롭다운/모달용
     =========================*/
    //제품리스트
    List<ProductVO> getProductList();
    //자재별공급처리스트
    List<MatVendorVO>getMatVendorList(String matId);
    //창고리스트
    List<WareHouseVO> getWarehouseList();

    /*======================================
    * 공통코드 상태값
    * ======================================*/
    List<MatStatusVO> selectCodeList(String groupId);
}
