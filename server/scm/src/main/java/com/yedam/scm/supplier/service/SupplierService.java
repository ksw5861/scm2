package com.yedam.scm.supplier.service;

import java.util.List;
import java.util.Map;

import com.yedam.scm.dto.ApproveMatSearchDTO;
import com.yedam.scm.dto.matSupplySearchDTO;
import com.yedam.scm.vo.InboundLogVO;
import com.yedam.scm.vo.InboundVO;
import com.yedam.scm.vo.PurchaseMatVO;

public interface SupplierService {
    //발주목록
    List<PurchaseMatVO> getMatOerderList(String vendorId, ApproveMatSearchDTO searchDTO);
    //발주승인
    int updateOrderApprove( Map<String, Object> data);
    //발주반려
    void updateOrderReject(Long purId, String rejMemo, String staff);
    //출고지시대기목록
    List<PurchaseMatVO> getMatWReleaseList(String vendorId, matSupplySearchDTO searchDTO);
    //출고지시(출고수량/출고예정일 입력)
    void insertReleaseData(List<PurchaseMatVO> payload);
    //출고승인목록 출력
    List<PurchaseMatVO> getApprovedShipmentList (String vendorId, ApproveMatSearchDTO searchDTO);
    //출고등록
    void insertShipmentInfo(InboundVO MatShipInfo);
    //공급(출고)목록
    List<InboundVO> getSupplyList(String vendorId, matSupplySearchDTO searchDTO);
    //출고상세목록
    List<InboundLogVO> getSupplyDetailList(Long inboundDetId);
    //대시보드
    Map<String, Object> getVendorDashboard(String vendorId);
    //주문내역조회
    List<PurchaseMatVO> getRequestResultList(String vendorId, matSupplySearchDTO searchDTO);

}
