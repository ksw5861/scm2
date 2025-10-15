// src/main/java/com/yedam/scm/outboundMat/service/impl/PlanServiceImpl.java
package com.yedam.scm.outboundMat.service.impl;

import com.yedam.scm.outboundMat.mapper.PlanMapper;
import com.yedam.scm.outboundMat.service.PlanService;
import com.yedam.scm.vo.PrdPlanDetailVO;
import com.yedam.scm.vo.ReqMatVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanMapper planMapper;

    @Override
    public List<PrdPlanDetailVO> getPlanDetails(Long plId, String prodNo, Date dateFrom, Date dateTo) {
        return planMapper.selectPlanDetails(plId, prodNo, dateFrom, dateTo);
    }

    @Override
    public int countPlanDetails(Long plId, String prodNo, Date dateFrom, Date dateTo) {
        return planMapper.countPlanDetails(plId, prodNo, dateFrom, dateTo);
    }

    @Override
    public List<ReqMatVO> getReqMatWithMaterialByPlDetId(Long plDetId) {
        return planMapper.selectReqMatWithMaterialByPlDetId(plDetId);
    }
}
