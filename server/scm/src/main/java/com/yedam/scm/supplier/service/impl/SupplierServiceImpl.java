package com.yedam.scm.supplier.service.impl;

import java.beans.Transient;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.scm.supplier.mapper.SupplierMapper;
import com.yedam.scm.supplier.service.SupplierService;
import com.yedam.scm.vo.PurchaseMatVO;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class SupplierServiceImpl implements SupplierService  {

    final SupplierMapper mapper;

    @Override
    public List<PurchaseMatVO> getMatOerderList(String vendorId) {
        return mapper.getMatOerderList(vendorId);
    }

    @Transactional
    @Override
    public int updateOrderApprove(Map<String, Object> data) {
       
        List<Integer> idList  = (List<Integer>) data.get("purId");
        String name = (String) data.get("name");

        int updatedCount = 0;

        System.out.println("리스트용" + idList);
        System.out.println("이름이세요" + name);


        for(Integer purId : idList ){
            updatedCount += mapper.updateOrderApprove(purId);
            mapper.insertStatusLog(purId, name);
        }
        return updatedCount;
    }
    
}
