package com.yedam.scm.order.service;

import java.util.List;
import java.util.Map;

public interface BranchDashService {
    
    List<Map<String, Object>> getSalesTrend(String vendorId, String range);
}
