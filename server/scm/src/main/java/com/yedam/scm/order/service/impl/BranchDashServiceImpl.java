package com.yedam.scm.order.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yedam.scm.order.mapper.BranchDashMapper;
import com.yedam.scm.order.service.BranchDashService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BranchDashServiceImpl implements BranchDashService {

    private final BranchDashMapper branchDashMapper;

    @Override
    public List<Map<String, Object>> getSalesTrend(String vendorId, String range) {
        switch (range) {
            case "daily":
                return branchDashMapper.selectDailySales(vendorId);
            case "weekly":
                return branchDashMapper.selectWeeklySales(vendorId);
            case "monthly":
                return branchDashMapper.selectMonthlySales(vendorId);
            case "yearly":
                return branchDashMapper.selectYearlySales(vendorId);
            default:
                throw new IllegalArgumentException("Invalid range: " + range);
        }
    }
}
