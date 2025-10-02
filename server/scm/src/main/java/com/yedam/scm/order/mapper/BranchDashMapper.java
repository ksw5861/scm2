package com.yedam.scm.order.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BranchDashMapper {
    List<Map<String, Object>> selectDailySales(String vendorId);
    List<Map<String, Object>> selectWeeklySales(String vendorId);
    List<Map<String, Object>> selectMonthlySales(String vendorId);
    List<Map<String, Object>> selectYearlySales(String vendorId);
}
