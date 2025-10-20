package com.yedam.scm.purchaseMat.mapper;

import java.sql.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.dto.PurchaseListSearchDTO;
import com.yedam.scm.vo.MatStatusVO;
import com.yedam.scm.vo.MatVendorVO;
import com.yedam.scm.vo.MrpDetailVO;
import com.yedam.scm.vo.PrdPlanDetailVO;
import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.ProductionPlanVO;
import com.yedam.scm.vo.PurStatusLogVO;
import com.yedam.scm.vo.PurchaseMatVO;
import com.yedam.scm.vo.WareHouseVO;

@Mapper
public interface PurchaseMatMapper {
    
    //생산계획등록[마스터 + 디테일]
    int insertProductionPlan(ProductionPlanVO master); //마스터
    int insertProductionPlanDetail(PrdPlanDetailVO detail); //디테일

    //제품생산계획리스트(모달)
    List<ProductionPlanVO> getPlanMasterList();
    // 생산계획 상세조회
    List<PrdPlanDetailVO> selectPlanDetailList(Long plId);
  
    //생산용 내부 자재요청insert프로시저
    void callInsertReqMatProc(Long plDetId);

    //mrp산출
    void callCalcMrpProc(@Param("plId") Long plId, @Param("empName") String empName);

    //mrp목록
    List<MrpDetailVO> getMrpList();

    //자재주문등록(공급처)
    void callReqestMatProc(PurchaseMatVO requestList);
   
    //자재주문목록
    List<PurchaseMatVO> getPurchaseList(@Param("page")PageDTO PageDTO, @Param("search")PurchaseListSearchDTO searchDTO);
    Long getPurchaseMasterCount(@Param("search")PurchaseListSearchDTO searchDTO);

    //자재주문상태목록
     List<PurStatusLogVO> getPurchaseStatus(Long purId);

    /*==========================
     * 드롭다운/모달용
     ===========================*/
    //제품리스트
    List<ProductVO> getProductList();

    //자재별 계약 거래처 리스트
    List<MatVendorVO> getMatVendorList(String matId);

    //창고리스트
    List<WareHouseVO> getWarehouseList();

    /*======================================
     * 공통코드 상태값
     * ======================================*/
    List<MatStatusVO> selectCodeList(String groupId);
}
