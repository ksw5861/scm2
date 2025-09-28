package com.yedam.scm.supplier.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.yedam.scm.vo.PurchaseMatVO;

@Mapper
public interface SupplierMapper {
    //주문목록
    List<PurchaseMatVO>getMatOerderList(String vendorId);
}
