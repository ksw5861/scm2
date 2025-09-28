package com.yedam.scm.supplier.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.yedam.scm.vo.PurchaseMatVO;

@Mapper
public interface SupplierMapper {
    //주문목록
    List<PurchaseMatVO>getMatOerderList(String vendorId);
    //주문승인상태변경
    int updateOrderApprove(Integer purId);
    //주문승인상태이력남김
    void insertStatusLog(@Param("purId")Integer purId, @Param("name")String name);
}
