package com.yedam.scm.supplier.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

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
    
}
