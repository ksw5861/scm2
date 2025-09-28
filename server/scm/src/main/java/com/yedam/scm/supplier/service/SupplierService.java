package com.yedam.scm.supplier.service;

import java.util.List;

import com.yedam.scm.vo.PurchaseMatVO;

public interface SupplierService {
    List<PurchaseMatVO> getMatOerderList(String vendorId);
}
