package com.yedam.scm.order.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface BranchDashMapper {
    
    List<Map<String, Object>> selectSalesTrend(@Param("vendorId") String vendorId,
                                               @Param("range") String range);
}
