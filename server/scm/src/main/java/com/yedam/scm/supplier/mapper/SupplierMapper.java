package com.yedam.scm.supplier.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.yedam.scm.vo.PurchaseMatVO;

@Mapper
public interface SupplierMapper {
    //주문목록
    List <PurchaseMatVO> getMatOerderList(String vendorId);
    //주문승인상태변경
    int updateOrderApprove(Integer purId);
    //주문승인상태이력남김
    void insertStatusLog(@Param("purId")int purId, @Param("name")String name);
    //출고지시대기목록
    List <PurchaseMatVO> getMatWReleaseList(String vendorId);
    //출고지시등록
    void callReleaseMatPoc(@Param("purId")Long purId, @Param("outQty")Long outQty, @Param("vendorId")String vendorId, @Param("expectDate")Date expectDate);
    //[공급처출고등록시: 1)입고마스터테이블 seq 2)입고마스터테이블insert 3)입고상세테이블insert 4)배송정보등록]
    //1)입고마스터테이블 GET'seq'
    Long getInboundMasterPK(); 
    //2)입고마스터테이블 입력
    int insertInboundMaster(@Param("inboundId")Long inboundMasterPK, @Param("vendorId")String vendorId);
    //3)입고상세테이블
    int insertInboundDetail(@Param("inboundId")Long inboundMasterPK, @Param("purId")Long purId, @Param("matId")String matId, @Param("outQty")Long outQty, @Param("vendorId")String vendorId);
    //4)배송정보테이블
}
