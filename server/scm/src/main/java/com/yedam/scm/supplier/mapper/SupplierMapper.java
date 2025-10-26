package com.yedam.scm.supplier.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.yedam.scm.dto.ApproveMatSearchDTO;
import com.yedam.scm.dto.VendorChartDTO;
import com.yedam.scm.dto.VendorDashboardSummaryDTO;
import com.yedam.scm.dto.VendorDefectDTO;
import com.yedam.scm.dto.VendorRecentListDTO;
import com.yedam.scm.dto.matSupplySearchDTO;
import com.yedam.scm.vo.InboundDetailVO;
import com.yedam.scm.vo.InboundLogVO;
import com.yedam.scm.vo.InboundVO;
import com.yedam.scm.vo.PurchaseMatVO;

@Mapper
public interface SupplierMapper {
    //발주목록
    List <PurchaseMatVO> getMatOerderList(@Param("vendorId")String vendorId, @Param("search")ApproveMatSearchDTO searchDTO);
    //발주승인상태변경
    int updateOrderApprove(Integer purId);
    //발주승인상태이력남김
    void insertStatusLog(@Param("purId")int purId,  @Param("venId")String vendorId, @Param("venName")String name);
    //발주반려
    void callUpdateOrderReject(@Param("p_pur_id")Long purId, @Param("p_rej_memo")String rejMemo, @Param("p_staff")String staff);
    //출고지시대기목록
    List <PurchaseMatVO> getMatWReleaseList(@Param("vendorId")String vendorId, @Param("search")matSupplySearchDTO searchDTO);
    //출고지시등록
    void callReleaseMatPoc(@Param("purId")Long purId, @Param("outQty")Long outQty, @Param("vendorId")String vendorId, @Param("expectDate")Date expectDate, @Param("vendorEmp")String vendorEmp);
    
    //출고등록화면
    //1)출고대기리스트
    List <PurchaseMatVO>getApprovedShipmentList(@Param("vendorId")String vendorId, @Param("search")ApproveMatSearchDTO searchDTO);
    
    //출고등록
    /*
    1. 입고마스터테이블 seq -> 입고마스터 insert / 출고정보 insert 
    2. 입고상세insert / 입고이력 insert / 기존발주테이블 요청수량 = 누적출고수량은 종결처리
     */
    //1)입고마스터테이블 GET'seq'
    Long getInboundMasterPK();
    //입고마스터 + 출고정보 insert 프로시저!
    void callShipmentMasterPoc(InboundVO MatShipInfo);
    //2) 프로시저
    void callShipmentDetailPoc(InboundDetailVO detail);

    //공급목록
    List<InboundVO> getSupplyList(@Param("vendorId")String vendorId, @Param("search")matSupplySearchDTO searchDTO);
    //출고상세목록
    List<InboundLogVO> getSupplyDetailList(Long inboundDetId);
    //대시보드smmary
    VendorDashboardSummaryDTO getVendorDashboardSummary(@Param("vendorId")String vendorId);
    //대시보드 최근 7건
    List<VendorRecentListDTO> getVendorRecentList(@Param("vendorId")String vendorId);
    //파이차트
    List<VendorChartDTO> getVendorChartData(@Param("vendorId")String vendorId);
    //불량목록
    List<VendorDefectDTO> getVendorDefectList(@Param("vendorId")String vendorId);
    //주문내역조회
    List<PurchaseMatVO> getRequestResultList(@Param("vendorId")String vendorId, @Param("search")matSupplySearchDTO searchDTO);
}
