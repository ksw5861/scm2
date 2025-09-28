package com.yedam.scm.supplier.service;

import java.util.List;
import java.util.Map;

import com.yedam.scm.vo.PurchaseMatVO;

public interface SupplierService {
    List<PurchaseMatVO> getMatOerderList(String vendorId);
    int updateOrderApprove( Map<String, Object> data);

}
