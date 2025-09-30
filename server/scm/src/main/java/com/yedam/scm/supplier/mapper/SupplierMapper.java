package com.yedam.scm.supplier.mapper;

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
    //출고대기목록
    List <PurchaseMatVO> getMatWReleaseList(String vendorId);
   
    //[공급처출고등록시: 1) 주문테이블 update + 상태값 변경(프로시저) 2)입고마스터테이블 seq 3)입고마스터테이블insert 4)입고상세테이블insert ]
    //1)출고정보등록
    void callReleaseMatPoc(@Param("purId")Long purId, @Param("outQty")Long outQty, @Param("name")String vendorId);
    //2)입고마스터테이블 GET'seq'
    Long getInboundMasterPK(); 
    //3)입고마스터테이블 입력
    int insertInboundMaster(@Param("inboundId")Long inboundMasterPK, @Param("vendorId")String vendorId);
    //4)입고상세테이블
    int insertInboundDetail(@Param("inboundId")Long inboundMasterPK, @Param("purId")Long purId, @Param("matId")String matId, @Param("outQty")Long outQty, @Param("vendorId")String vendorId);
}
