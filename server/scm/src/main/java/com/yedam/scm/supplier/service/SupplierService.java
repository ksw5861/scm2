package com.yedam.scm.supplier.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.yedam.scm.vo.PurchaseMatVO;

public interface SupplierService {
    //주문목록
    List<PurchaseMatVO> getMatOerderList(String vendorId);
    //주문승인
    int updateOrderApprove( Map<String, Object> data);
    //출고지시대기목록
    List<PurchaseMatVO> getMatWReleaseList(String vendorId);
    //출고지시
    void insertReleaseData(List<PurchaseMatVO> payload);
    //출고등록

}
