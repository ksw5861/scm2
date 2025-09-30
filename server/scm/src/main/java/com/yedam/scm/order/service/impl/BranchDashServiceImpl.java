package com.yedam.scm.order.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.scm.order.mapper.BranchDashMapper;
import com.yedam.scm.order.service.BranchDashService;

@Service
public class BranchDashServiceImpl implements BranchDashService {

    @Autowired
    private BranchDashMapper branchDashMapper;

    @Override
    public List<Map<String, Object>> getSalesTrend(String vendorId, String range) {
        return branchDashMapper.selectSalesTrend(vendorId, range);
    }
}
